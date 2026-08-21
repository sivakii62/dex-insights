package com.dex.insights.repository;

import com.dex.insights.domain.Incident;

import java.util.List;

/** Read access to incidents, indexed by store. */
public interface IncidentRepository {

    List<Incident> findAll();

    List<Incident> findByStoreId(String storeId);
}
