package com.dex.insights.repository;

import com.dex.insights.domain.Transaction;

import java.util.List;

/** Read access to transactions, indexed by store. */
public interface TransactionRepository {

    List<Transaction> findAll();

    List<Transaction> findByStoreId(String storeId);
}
