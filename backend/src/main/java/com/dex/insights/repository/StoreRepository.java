package com.dex.insights.repository;

import com.dex.insights.domain.Store;

import java.util.List;
import java.util.Optional;

/** Read access to store snapshots. An interface so the in-memory source can be swapped for a datastore. */
public interface StoreRepository {

    List<Store> findAll();

    Optional<Store> findById(String storeId);
}
