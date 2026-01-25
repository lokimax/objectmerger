package de.x132.objectmerger.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.x132.objectmerger.service.ObjectMergerService;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Reproduction Test for 400 Error")
class ReproductionTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  // Mock the service so we don't need a real one for this test
  @MockBean private ObjectMergerService mergerService;

  @Test
  @DisplayName("Should return 200 for Person class (Allowed)")
  void testGeneratePersonKey() throws Exception {
    Map<String, String> request = Map.of("className", "Person");

    mockMvc
        .perform(
            post("/api/v1/merge/generator/class")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should return 200 for String class (Currently 400)")
  void testGenerateStringClass() throws Exception {
    Map<String, String> request = Map.of("className", "java.lang.String");

    // This is expected to fail with 400 before the fix
    mockMvc
        .perform(
            post("/api/v1/merge/generator/class")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}
