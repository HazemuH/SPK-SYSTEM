package com.spkmainan.publicapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.spkmainan.ahp.SawEngine;
import com.spkmainan.calculation.CalculationRunRepository;
import com.spkmainan.domain.DomainCatalog;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** With no published run, the public API returns empty — the gate is closed. */
class CatalogServiceEmptyTest {

    private CatalogService serviceWithNoPublishedRun() {
        CalculationRunRepository runs = mock(CalculationRunRepository.class);
        when(runs.findFirstByPublishedTrueOrderByPublishedAtDesc()).thenReturn(Optional.empty());
        DomainCatalog catalog = mock(DomainCatalog.class);
        return new CatalogService(catalog, new SawEngine(), runs);
    }

    @Test
    void top_isEmpty_whenNothingPublished() {
        assertThat(serviceWithNoPublishedRun().top("balanced", 5)).isEmpty();
    }

    @Test
    void detail_isNull_whenNothingPublished() {
        assertThat(serviceWithNoPublishedRun().detail(1)).isNull();
    }

    @Test
    void recommend_isEmpty_whenNothingPublished() {
        var rec = serviceWithNoPublishedRun().recommend("3-5", "300000", "edukatif", "safety");
        assertThat(rec.primary()).isEmpty();
        assertThat(rec.others()).isEmpty();
    }

    @Test
    void compare_isEmpty_whenNothingPublished() {
        var cmp = serviceWithNoPublishedRun().compare(List.of(1, 2, 3), "balanced");
        assertThat(cmp.toys()).isEmpty();
    }
}
