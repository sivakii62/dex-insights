package com.dex.insights.repository;

import com.dex.insights.domain.Dataset;
import com.dex.insights.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Serves transactions from the startup snapshot, grouped by store for O(1) lookup. */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> transactions;
    private final Map<String, List<Transaction>> byStoreId;

    public InMemoryTransactionRepository(Dataset dataset) {
        this.transactions = dataset.transactions();
        this.byStoreId = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::storeId,
                        Collectors.collectingAndThen(Collectors.toList(), List::copyOf)));
    }

    @Override
    public List<Transaction> findAll() {
        return transactions;
    }

    @Override
    public List<Transaction> findByStoreId(String storeId) {
        return byStoreId.getOrDefault(storeId, List.of());
    }
}
