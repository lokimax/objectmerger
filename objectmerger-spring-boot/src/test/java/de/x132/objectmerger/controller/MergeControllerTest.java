package de.x132.objectmerger.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.x132.objectmerger.security.ClassLoadingGuard;
import de.x132.objectmerger.service.ObjectMergerService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MergeController.class)
@Import(ClassLoadingGuard.class)
class MergeControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ObjectMergerService objectMergerService;

    @Test
    void testGenerateFromClass() throws Exception {
        Map<String, String> request = Map.of("className", "Person");

        mockMvc.perform(
                        post("/api/v1/merge/generator/class")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definitions.name").exists());
    }

    @Test
    void testGenerateFromClass_Invalid() throws Exception {
        Map<String, String> request = Map.of("className", "UnknownClass");

        mockMvc.perform(
                        post("/api/v1/merge/generator/class")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGenerateFromClass_DangerousClassIsBlocked() throws Exception {
        Map<String, String> request = Map.of("className", "java.lang.Runtime");

        mockMvc.perform(
                        post("/api/v1/merge/generator/class")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGenerateFromJson() throws Exception {
        Map<String, Object> request = Map.of("field1", "val1", "field2", 123);

        mockMvc.perform(
                        post("/api/v1/merge/generator/json")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definitions.field1").exists())
                .andExpect(jsonPath("$.definitions.field2").exists());
    }
}
