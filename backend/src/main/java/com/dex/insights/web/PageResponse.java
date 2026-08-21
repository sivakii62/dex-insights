package com.dex.insights.web;

import java.util.List;

/** A single page of results. Slicing happens in memory: the dataset is small and fully resident. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static <T> PageResponse<T> of(List<T> all, int page, int size) {
        int totalPages = (int) Math.ceil((double) all.size() / size);
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<T> content = List.copyOf(all.subList(from, to));
        return new PageResponse<>(content, page, size, all.size(), totalPages, to < all.size());
    }
}
