package com.dex.insights;

import com.dex.insights.web.ApiPaths;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exercises the real endpoints against the bundled dataset: wiring, JSON shape and error contract. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsStoresSortedByOfflinePumpsDescending() throws Exception {
        mockMvc.perform(get(ApiPaths.STORES)
                        .param("sortBy", "OFFLINE_PUMPS")
                        .param("direction", "DESC")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].storeId").value("10004"))
                .andExpect(jsonPath("$.content[0].offlinePumps").value(12))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void filtersStoresByBrandAndStatus() throws Exception {
        mockMvc.perform(get(ApiPaths.STORES).param("brand", "speedway").param("status", "ONLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].brand").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.equalTo("Speedway"))));
    }

    @Test
    void returnsAProblemDetailForAnUnknownStore() throws Exception {
        mockMvc.perform(get(ApiPaths.STORES + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.identifier").value("99999"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void rejectsAnUnknownStatusWithAHelpfulProblemDetail() throws Exception {
        mockMvc.perform(get(ApiPaths.STORES).param("status", "BROKEN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("ONLINE")));
    }

    @Test
    void returnsTheOperationalOverview() throws Exception {
        mockMvc.perform(get(ApiPaths.INSIGHTS + "/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fleet.totalStores").value(10))
                .andExpect(jsonPath("$.topStoresByOfflinePumps[0].storeId").value("10004"))
                .andExpect(jsonPath("$.tankRunoutRisks").isNotEmpty())
                .andExpect(jsonPath("$.incidents.total").value(8))
                .andExpect(jsonPath("$.incidents.bySeverity.HIGH").value(3));
    }

    @Test
    void answersAGroundedQuestionWithCitations() throws Exception {
        mockMvc.perform(post(ApiPaths.CHAT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "question", "Summarize store 10001 health and recent activity",
                                "storeId", "10001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("Store 10001")))
                .andExpect(jsonPath("$.citations").isNotEmpty())
                .andExpect(jsonPath("$.citations[0].recordId").exists())
                .andExpect(jsonPath("$.citations[*].storeId").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.equalTo("10001"))))
                .andExpect(jsonPath("$.retrievedContextSummary").isNotEmpty());
    }

    @Test
    void rejectsABlankQuestion() throws Exception {
        mockMvc.perform(post(ApiPaths.CHAT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.errors.question").exists());
    }

    @Test
    void exposesTheOpenApiDescription() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Dex Insights API"))
                .andExpect(jsonPath("$.paths['" + ApiPaths.CHAT + "']").exists());
    }
}
