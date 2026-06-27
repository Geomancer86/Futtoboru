package com.rndmodgames.futtoboru.engine.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;

class NewsLatencyResolverTest extends BaseTest {

    @Test
    void sameRegionIsInstant() {
        NewsLatencyResolver resolver = new NewsLatencyResolver();
        assertEquals(0, resolver.deliveryDays(
            NewsLatencyResolver.BRITISH_ISLES,
            NewsLatencyResolver.BRITISH_ISLES,
            1888));
    }

    @Test
    void transatlanticDelayAtLeastTwoWeeksIn1888() {
        NewsLatencyResolver resolver = new NewsLatencyResolver();
        int days = resolver.deliveryDays(
            NewsLatencyResolver.SOUTH_AMERICA,
            NewsLatencyResolver.BRITISH_ISLES,
            1888);
        assertTrue(days >= 14);
    }

    @Test
    void deliveryDateAddsLatencyDays() {
        NewsLatencyResolver resolver = new NewsLatencyResolver();
        LocalDateTime event = LocalDateTime.of(1892, 1, 15, 9, 0);
        LocalDateTime delivered = resolver.deliveryDate(
            event,
            NewsLatencyResolver.SOUTH_AMERICA,
            NewsLatencyResolver.BRITISH_ISLES);
        assertEquals(event.plusDays(21), delivered);
    }
}
