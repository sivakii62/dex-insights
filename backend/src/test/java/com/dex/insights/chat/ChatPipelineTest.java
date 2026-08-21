package com.dex.insights.chat;

import com.dex.insights.domain.Dataset;
import com.dex.insights.repository.InMemoryIncidentRepository;
import com.dex.insights.repository.InMemoryStoreRepository;
import com.dex.insights.repository.InMemoryTransactionRepository;
import com.dex.insights.support.TestData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Covers retrieval, context assembly and deterministic generation as one pipeline. */
class ChatPipelineTest {

    private final Dataset dataset = TestData.dataset();
    private final ChatProperties properties = new ChatProperties(6, 4000, null);
    private final DocumentIndex index = new DocumentIndex(
            new InMemoryStoreRepository(dataset),
            new InMemoryIncidentRepository(dataset),
            new InMemoryTransactionRepository(dataset));
    private final DocumentRetriever retriever = new DocumentRetriever(index);
    private final ChatService chatService = new ChatService(index, retriever, new ContextAssembler(properties),
            new TemplateAnswerGenerator(new InMemoryStoreRepository(dataset),
                    new InMemoryIncidentRepository(dataset)),
            properties);

    @Test
    void indexesEverySourceRecordAsADocument() {
        assertThat(index.documents()).hasSize(8); // 3 stores + 3 incidents + 2 transactions
        assertThat(index.incidentsForStore("10004")).extracting(RetrievableDocument::id)
                .containsExactly("INC-10004-0001");
    }

    @Test
    void boostsDocumentsForAStoreIdMentionedInTheQuestion() {
        List<ScoredDocument> retrieved = retriever.retrieve("Summarize store 10009 health", null, 5);

        assertThat(retrieved).isNotEmpty();
        assertThat(retrieved.getFirst().document().storeId()).isEqualTo("10009");
    }

    @Test
    void restrictsRetrievalToTheRequestedStore() {
        List<ScoredDocument> retrieved = retriever.retrieve("pump status", "10001", 10);

        assertThat(retrieved).isNotEmpty();
        assertThat(retrieved).allSatisfy(scored ->
                assertThat(scored.document().storeId()).isEqualTo("10001"));
    }

    @Test
    void answersOfflinePumpQuestionsWorstStoreFirstAndCitesTheRecords() {
        ChatResponse response = chatService.answer(new ChatRequest(
                "Which stores have the highest offline pumps and what incidents are associated with them?", null));

        assertThat(response.answer())
                .startsWith("Ranked by offline pumps")
                .contains("12 of 12 pumps offline")
                .contains("INC-10004-0001");
        assertThat(response.answer().substring(response.answer().indexOf("Store 10")))
                .startsWith("Store 10004");
        assertThat(response.citations()).isNotEmpty()
                .anySatisfy(citation -> assertThat(citation.type()).isEqualTo(DocumentType.STORE))
                .anySatisfy(citation -> assertThat(citation.type()).isEqualTo(DocumentType.INCIDENT));
        assertThat(response.retrievedContextSummary()).contains("Retrieved");
    }

    @Test
    void summarizesASingleStoreFromItsOwnRecordsOnly() {
        ChatResponse response = chatService.answer(
                new ChatRequest("Summarize store 10001 health and recent activity", "10001"));

        assertThat(response.answer()).contains("Store 10001").contains("0 of 8 pumps offline");
        assertThat(response.citations()).isNotEmpty()
                .allSatisfy(citation -> assertThat(citation.storeId()).isEqualTo("10001"));
    }

    @Test
    void identifiesTanksThatLookLikeARunoutRisk() {
        ChatResponse response = chatService.answer(
                new ChatRequest("Any stores with low tank levels that look like runout risk?", null));

        assertThat(response.answer()).contains("runout risk");
        assertThat(response.answer().substring(response.answer().indexOf("Store 10")))
                .startsWith("Store 10009");
    }

    @Test
    void saysSoWhenNothingInTheDatasetMatches() {
        ChatResponse response = chatService.answer(new ChatRequest("zzzz qqqq wwww", null));

        assertThat(response.citations()).isEmpty();
        assertThat(response.answer()).contains("No records in the dataset match");
        assertThat(response.retrievedContextSummary()).contains("No dataset records matched");
    }

    @Test
    void capsAssembledContextAtTheConfiguredCharacterBudget() {
        ContextAssembler tinyBudget = new ContextAssembler(new ChatProperties(6, 50, null));
        AssembledContext context = tinyBudget.assemble(retriever.retrieve("store 10004 offline pumps", null, 6));

        assertThat(context.citations()).hasSize(1);
    }
}
