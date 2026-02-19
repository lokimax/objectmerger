package de.x132.objectmerger.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.x132.objectmerger.dto.LabeledSourceDTO;
import de.x132.objectmerger.dto.MergeRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("MergeController Integration Tests")
class MergeControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @DisplayName("Should return UP status from health endpoint")
        void testHealthEndpoint() throws Exception {
                mockMvc.perform(get("/api/v1/merge/health"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("Should merge person from example endpoint")
        void testExampleEndpoint() throws Exception {
                mockMvc.perform(post("/api/v1/merge/example"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Max Müller"))
                                .andExpect(jsonPath("$.age").value(35))
                                .andExpect(jsonPath("$.email").value("max@example.com"))
                                .andExpect(jsonPath("$.phone").value("030-654321"));
        }

        @Test
        @DisplayName("Should merge person with priority strategy")
        void testMergeWithPriorityStrategy() throws Exception {
                // Prepare request
                MergeRequest request = new MergeRequest();
                request.setTargetClass("de.x132.objectmerger.model.Person");

                // Define merge definition with priority
                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put(
                                "name",
                                Map.of("strategy", "priority", "priority", Map.of("database", 1, "crm", 2)));
                definition.put("age", Map.of("strategy", "maximum"));
                definition.put("email", Map.of("strategy", "priority", "priority", Map.of("database", 1)));
                definition.put("phone", Map.of("strategy", "priority", "priority", Map.of("crm", 1)));
                request.setDefinition(definition);

                // Define sources
                Map<String, Object> dbSource = new LinkedHashMap<>();
                dbSource.put("name", "Max Müller");
                dbSource.put("age", 30);
                dbSource.put("email", "max@example.com");
                dbSource.put("phone", null);

                Map<String, Object> crmSource = new LinkedHashMap<>();
                crmSource.put("name", "Maximilian Müller");
                crmSource.put("age", 25);
                crmSource.put("email", null);
                crmSource.put("phone", "030-123456");

                List<LabeledSourceDTO> sources = Arrays.asList(
                                new LabeledSourceDTO("database", dbSource),
                                new LabeledSourceDTO("crm", crmSource));
                request.setSources(sources);

                // Execute request
                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Max Müller")) // priority 1: database
                                .andExpect(jsonPath("$.age").value(30)) // maximum strategy
                                .andExpect(jsonPath("$.email").value("max@example.com")) // priority 1: database
                                .andExpect(jsonPath("$.phone").value("030-123456")); // priority 1: crm
        }

        @Test
        @DisplayName("Should merge with maximum strategy")
        void testMergeWithMaximumStrategy() throws Exception {
                MergeRequest request = new MergeRequest();
                request.setTargetClass("de.x132.objectmerger.model.Person");

                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put("name", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("age", Map.of("strategy", "maximum"));
                definition.put("email", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("phone", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                request.setDefinition(definition);

                Map<String, Object> source1 = new LinkedHashMap<>();
                source1.put("name", "John");
                source1.put("age", 25);
                source1.put("email", "john@example.com");
                source1.put("phone", "111");

                Map<String, Object> source2 = new LinkedHashMap<>();
                source2.put("name", "Jane");
                source2.put("age", 35);
                source2.put("email", "jane@example.com");
                source2.put("phone", "222");

                Map<String, Object> source3 = new LinkedHashMap<>();
                source3.put("name", "Bob");
                source3.put("age", 30);
                source3.put("email", "bob@example.com");
                source3.put("phone", "333");

                List<LabeledSourceDTO> sources = Arrays.asList(
                                new LabeledSourceDTO("source1", source1),
                                new LabeledSourceDTO("source2", source2),
                                new LabeledSourceDTO("source3", source3));
                request.setSources(sources);

                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.age").value(35)); // maximum of 25, 35, 30
        }

        @Test
        @DisplayName("Should merge with minimum strategy")
        void testMergeWithMinimumStrategy() throws Exception {
                MergeRequest request = new MergeRequest();
                request.setTargetClass("de.x132.objectmerger.model.Person");

                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put("name", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("age", Map.of("strategy", "minimum"));
                definition.put("email", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("phone", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                request.setDefinition(definition);

                Map<String, Object> source1 = new LinkedHashMap<>();
                source1.put("name", "Person1");
                source1.put("age", 25);
                source1.put("email", "p1@example.com");
                source1.put("phone", "111");

                Map<String, Object> source2 = new LinkedHashMap<>();
                source2.put("name", "Person2");
                source2.put("age", 35);
                source2.put("email", "p2@example.com");
                source2.put("phone", "222");

                List<LabeledSourceDTO> sources = Arrays.asList(
                                new LabeledSourceDTO("source1", source1),
                                new LabeledSourceDTO("source2", source2));
                request.setSources(sources);

                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.age").value(25)); // minimum of 25, 35
        }

        @Test
        @DisplayName("Should merge with average strategy")
        void testMergeWithAverageStrategy() throws Exception {
                MergeRequest request = new MergeRequest();
                request.setTargetClass("de.x132.objectmerger.model.Person");

                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put("name", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("age", Map.of("strategy", "average"));
                definition.put("email", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("phone", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                request.setDefinition(definition);

                Map<String, Object> source1 = new LinkedHashMap<>();
                source1.put("name", "Person1");
                source1.put("age", 20);
                source1.put("email", "p1@example.com");
                source1.put("phone", "111");

                Map<String, Object> source2 = new LinkedHashMap<>();
                source2.put("name", "Person2");
                source2.put("age", 30);
                source2.put("email", "p2@example.com");
                source2.put("phone", "222");

                Map<String, Object> source3 = new LinkedHashMap<>();
                source3.put("name", "Person3");
                source3.put("age", 40);
                source3.put("email", "p3@example.com");
                source3.put("phone", "333");

                List<LabeledSourceDTO> sources = Arrays.asList(
                                new LabeledSourceDTO("source1", source1),
                                new LabeledSourceDTO("source2", source2),
                                new LabeledSourceDTO("source3", source3));
                request.setSources(sources);

                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.age").value(30)); // average of 20, 30, 40
        }

        @Test
        @DisplayName("Should merge with sum strategy")
        void testMergeWithSumStrategy() throws Exception {
                MergeRequest request = new MergeRequest();
                request.setTargetClass("de.x132.objectmerger.model.Person");

                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put("name", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("age", Map.of("strategy", "sum"));
                definition.put("email", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                definition.put("phone", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                request.setDefinition(definition);

                Map<String, Object> source1 = new LinkedHashMap<>();
                source1.put("name", "Person1");
                source1.put("age", 10);
                source1.put("email", "p1@example.com");
                source1.put("phone", "111");

                Map<String, Object> source2 = new LinkedHashMap<>();
                source2.put("name", "Person2");
                source2.put("age", 20);
                source2.put("email", "p2@example.com");
                source2.put("phone", "222");

                Map<String, Object> source3 = new LinkedHashMap<>();
                source3.put("name", "Person3");
                source3.put("age", 30);
                source3.put("email", "p3@example.com");
                source3.put("phone", "333");

                List<LabeledSourceDTO> sources = Arrays.asList(
                                new LabeledSourceDTO("source1", source1),
                                new LabeledSourceDTO("source2", source2),
                                new LabeledSourceDTO("source3", source3));
                request.setSources(sources);

                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.age").value(60)); // sum of 10, 20, 30
        }

        @Test
        @DisplayName("Should return 403 when target class is not in allowed packages")
        void testMergeWithBlockedTargetClass() throws Exception {
                MergeRequest request = new MergeRequest();
                request.setTargetClass("com.invalid.NonExistentClass");

                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put("name", Map.of("strategy", "priority", "priority", Map.of("source1", 1)));
                request.setDefinition(definition);

                Map<String, Object> source = new LinkedHashMap<>();
                source.put("name", "Test");

                List<LabeledSourceDTO> sources = Collections.singletonList(new LabeledSourceDTO("source1", source));
                request.setSources(sources);

                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("Should handle null values in sources")
        void testMergeWithNullValues() throws Exception {
                MergeRequest request = new MergeRequest();
                request.setTargetClass("de.x132.objectmerger.model.Person");

                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put(
                                "name",
                                Map.of("strategy", "priority", "priority", Map.of("source1", 1, "source2", 2)));
                definition.put(
                                "age",
                                Map.of("strategy", "priority", "priority", Map.of("source1", 1, "source2", 2)));
                definition.put(
                                "email",
                                Map.of("strategy", "priority", "priority", Map.of("source1", 1, "source2", 2)));
                definition.put(
                                "phone",
                                Map.of("strategy", "priority", "priority", Map.of("source1", 1, "source2", 2)));
                request.setDefinition(definition);

                // source1 has nulls
                Map<String, Object> source1 = new LinkedHashMap<>();
                source1.put("name", null);
                source1.put("age", null);
                source1.put("email", "email1@example.com");
                source1.put("phone", null);

                // source2 fills in the gaps
                Map<String, Object> source2 = new LinkedHashMap<>();
                source2.put("name", "Fallback Name");
                source2.put("age", 40);
                source2.put("email", "email2@example.com");
                source2.put("phone", "999-999");

                List<LabeledSourceDTO> sources = Arrays.asList(
                                new LabeledSourceDTO("source1", source1),
                                new LabeledSourceDTO("source2", source2));
                request.setSources(sources);

                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Fallback Name")) // falls back to source2
                                .andExpect(jsonPath("$.age").value(40)) // falls back to source2
                                .andExpect(jsonPath("$.email").value("email1@example.com")) // source1 has value
                                .andExpect(jsonPath("$.phone").value("999-999")); // falls back to source2
        }

        @Test
        @DisplayName("Should merge with multiple sources and complex priority")
        void testMergeWithComplexPriority() throws Exception {
                MergeRequest request = new MergeRequest();
                request.setTargetClass("de.x132.objectmerger.model.Person");

                Map<String, Map<String, Object>> definition = new LinkedHashMap<>();
                definition.put(
                                "name",
                                Map.of(
                                                "strategy",
                                                "priority",
                                                "priority",
                                                Map.of("database", 1, "crm", 2, "analytics", 3)));
                definition.put("age", Map.of("strategy", "maximum"));
                definition.put(
                                "email",
                                Map.of(
                                                "strategy",
                                                "priority",
                                                "priority",
                                                Map.of("analytics", 1, "crm", 2, "database", 3)));
                definition.put(
                                "phone",
                                Map.of(
                                                "strategy",
                                                "priority",
                                                "priority",
                                                Map.of("crm", 1, "database", 2, "analytics", 3)));
                request.setDefinition(definition);

                Map<String, Object> dbSource = new LinkedHashMap<>();
                dbSource.put("name", "DB Name");
                dbSource.put("age", 25);
                dbSource.put("email", "db@example.com");
                dbSource.put("phone", "111-111");

                Map<String, Object> crmSource = new LinkedHashMap<>();
                crmSource.put("name", null);
                crmSource.put("age", 30);
                crmSource.put("email", "crm@example.com");
                crmSource.put("phone", "222-222");

                Map<String, Object> analyticsSource = new LinkedHashMap<>();
                analyticsSource.put("name", null);
                analyticsSource.put("age", 35);
                analyticsSource.put("email", "analytics@example.com");
                analyticsSource.put("phone", null);

                List<LabeledSourceDTO> sources = Arrays.asList(
                                new LabeledSourceDTO("database", dbSource),
                                new LabeledSourceDTO("crm", crmSource),
                                new LabeledSourceDTO("analytics", analyticsSource));
                request.setSources(sources);

                mockMvc.perform(
                                post("/api/v1/merge")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("DB Name")) // database has priority 1
                                .andExpect(jsonPath("$.age").value(35)) // maximum strategy
                                .andExpect(
                                                jsonPath("$.email")
                                                                .value("analytics@example.com")) // analytics has
                                                                                                 // priority 1
                                .andExpect(jsonPath("$.phone").value("222-222")); // crm has priority 1
        }
}
