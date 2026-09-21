package com.eyrax.apothicarsenal.client;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
class TooltipPaginationTest {
    @Test void preservesOrderAndEveryEntryWithinHeightBudget() {
        var input = IntStream.rangeClosed(1, 40).boxed().toList();
        var pages = TooltipPagination.partition(input, 50, n -> 10);
        assertEquals(8, pages.size());
        assertEquals(input, pages.stream().flatMap(List::stream).toList());
        assertTrue(pages.stream().allMatch(p -> p.size() * 10 <= 50));
    }
    @Test void oversizedVisualComponentRemainsIntactOnOwnPage() {
        var input = List.of(10, 300, 10, 10);
        assertEquals(List.of(List.of(10), List.of(300), List.of(10, 10)), TooltipPagination.partition(input, 100, n -> n));
        assertTrue(TooltipPagination.partition(List.<Integer>of(), 100, n -> n).isEmpty());
    }
}
