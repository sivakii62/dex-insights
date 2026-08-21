package com.dex.insights.store;

import com.dex.insights.domain.Store;
import com.dex.insights.domain.StoreStatus;
import com.dex.insights.repository.StoreRepository;
import com.dex.insights.web.PageResponse;
import com.dex.insights.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

/** Filtering, sorting and paging over the in-memory store snapshot. */
@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * @param brand  case-insensitive exact brand match, or null for all brands
     * @param status operational status filter, or null for all statuses
     */
    public PageResponse<Store> search(String brand, StoreStatus status, StoreSortField sortBy,
                                      SortDirection direction, int page, int size) {
        Comparator<Store> comparator = sortBy.comparator();
        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        List<Store> matches = storeRepository.findAll().stream()
                .filter(store -> !StringUtils.hasText(brand) || brand.equalsIgnoreCase(store.brand()))
                .filter(store -> status == null || status == store.status())
                .sorted(comparator)
                .toList();

        return PageResponse.of(matches, page, size);
    }

    public Store getById(String storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
    }
}
