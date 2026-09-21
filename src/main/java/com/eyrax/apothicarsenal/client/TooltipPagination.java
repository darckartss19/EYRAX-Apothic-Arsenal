package com.eyrax.apothicarsenal.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/** Stable, lossless partitioning; an oversized custom component remains intact. */
public final class TooltipPagination {
    public static <T> List<List<T>> partition(List<T> entries, int budget, ToIntFunction<T> height) {
        var pages = new ArrayList<List<T>>();
        var page = new ArrayList<T>();
        int used = 0;
        for (T entry : entries) {
            int size = Math.max(1, height.applyAsInt(entry));
            if (!page.isEmpty() && used + size > budget) {
                pages.add(List.copyOf(page));
                page.clear();
                used = 0;
            }
            page.add(entry);
            used += size;
        }
        if (!page.isEmpty()) pages.add(List.copyOf(page));
        return pages;
    }
    private TooltipPagination() {}
}
