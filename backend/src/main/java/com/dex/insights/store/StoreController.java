package com.dex.insights.store;

import com.dex.insights.domain.Store;
import com.dex.insights.domain.StoreStatus;
import com.dex.insights.web.ApiPaths;
import com.dex.insights.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.STORES)
@Validated
@Tag(name = "Stores", description = "Store operational status snapshots")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    @Operation(summary = "List stores",
            description = "Filter by brand and status, sort by any supported field, and page through results.")
    public PageResponse<Store> listStores(
            @Parameter(description = "Case-insensitive substring match against the brand name") @RequestParam(required = false) String brand,
            @Parameter(description = "Operational status") @RequestParam(required = false) StoreStatus status,
            @RequestParam(defaultValue = "STORE_ID") StoreSortField sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return storeService.search(brand, status, sortBy, direction, page, size);
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "Get a single store", description = "Returns 404 when the store id is unknown.")
    public Store getStore(@PathVariable @NotBlank String storeId) {
        return storeService.getById(storeId);
    }
}
