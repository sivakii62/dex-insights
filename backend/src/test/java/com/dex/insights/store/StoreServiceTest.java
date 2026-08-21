package com.dex.insights.store;

import com.dex.insights.domain.Store;
import com.dex.insights.domain.StoreStatus;
import com.dex.insights.repository.InMemoryStoreRepository;
import com.dex.insights.support.TestData;
import com.dex.insights.web.PageResponse;
import com.dex.insights.web.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreServiceTest {

    private final StoreService service = new StoreService(new InMemoryStoreRepository(TestData.dataset()));

    @Test
    void sortsByOfflinePumpsDescending() {
        PageResponse<Store> page = service.search(null, null, StoreSortField.OFFLINE_PUMPS,
                SortDirection.DESC, 0, 20);

        assertThat(page.content()).extracting(Store::storeId).containsExactly("10004", "10009", "10001");
    }

    @Test
    void filtersByBrandCaseInsensitivelyAndByStatus() {
        assertThat(service.search("speedway", null, StoreSortField.STORE_ID, SortDirection.ASC, 0, 20).content())
                .extracting(Store::storeId).containsExactly("10004", "10009");

        assertThat(service.search(null, StoreStatus.ONLINE, StoreSortField.STORE_ID, SortDirection.ASC, 0, 20)
                .content()).extracting(Store::storeId).containsExactly("10001");
    }

    @Test
    void filtersByBrandAsAPartialSubstringMatch() {
        assertThat(service.search("7", null, StoreSortField.STORE_ID, SortDirection.ASC, 0, 20).content())
                .extracting(Store::storeId).containsExactly("10001");
    }

    @Test
    void pagesResultsAndReportsWhetherMoreRemain() {
        PageResponse<Store> first = service.search(null, null, StoreSortField.STORE_ID, SortDirection.ASC, 0, 2);
        PageResponse<Store> second = service.search(null, null, StoreSortField.STORE_ID, SortDirection.ASC, 1, 2);

        assertThat(first.content()).hasSize(2);
        assertThat(first.totalElements()).isEqualTo(3);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.hasNext()).isTrue();
        assertThat(second.content()).hasSize(1);
        assertThat(second.hasNext()).isFalse();
    }

    @Test
    void reportsAnUnknownStoreAsNotFound() {
        assertThat(service.getById("10001").brand()).isEqualTo("7-Eleven");
        assertThatThrownBy(() -> service.getById("99999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99999");
    }
}
