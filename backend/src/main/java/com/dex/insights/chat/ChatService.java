package com.dex.insights.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The retrieve → expand → assemble → generate pipeline behind {@code POST /v1/chat}.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    /** Ceiling on incident documents pulled in by context expansion. */
    private static final int MAX_LINKED_INCIDENTS = 6;

    private final DocumentIndex documentIndex;
    private final DocumentRetriever retriever;
    private final ContextAssembler assembler;
    private final AnswerGenerator answerGenerator;
    private final ChatProperties properties;

    public ChatService(DocumentIndex documentIndex, DocumentRetriever retriever, ContextAssembler assembler,
                       AnswerGenerator answerGenerator, ChatProperties properties) {
        this.documentIndex = documentIndex;
        this.retriever = retriever;
        this.assembler = assembler;
        this.answerGenerator = answerGenerator;
        this.properties = properties;
    }

    public ChatResponse answer(ChatRequest request) {
        List<ScoredDocument> retrieved = retriever.retrieve(request.question(), request.storeId(),
                properties.topK());
        AssembledContext context = assembler.assemble(expandWithLinkedIncidents(retrieved));

        String answer = answerGenerator.generate(request, context);
        log.debug("Answered question with {} citation(s)", context.citations().size());

        return new ChatResponse(answer, context.citations(), context.summary());
    }

    /**
     * Pulls in the incidents belonging to a retrieved store.
     *
     * <p>Lexical retrieval finds the store that matches "highest offline pumps" but not necessarily
     * the incident records that explain it. Expanding along the store relationship supplies them,
     * and because they enter the context they are cited like any other retrieved record.
     */
    private List<ScoredDocument> expandWithLinkedIncidents(List<ScoredDocument> retrieved) {
        Set<String> present = new HashSet<>();
        retrieved.forEach(scored -> present.add(scored.document().id()));

        List<ScoredDocument> expanded = new ArrayList<>(retrieved);
        int added = 0;
        for (ScoredDocument scored : retrieved) {
            if (scored.document().type() != DocumentType.STORE) {
                continue;
            }
            for (RetrievableDocument incident : documentIndex.incidentsForStore(scored.document().storeId())) {
                if (added >= MAX_LINKED_INCIDENTS) {
                    return List.copyOf(expanded);
                }
                if (present.add(incident.id())) {
                    expanded.add(new ScoredDocument(incident, 0d));
                    added++;
                }
            }
        }
        return List.copyOf(expanded);
    }
}
