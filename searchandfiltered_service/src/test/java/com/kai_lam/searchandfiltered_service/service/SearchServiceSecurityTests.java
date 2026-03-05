package com.kai_lam.searchandfiltered_service.service;

import com.kai_lam.searchandfiltered_service.dto.SearchDocumentUpsertRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SearchServiceSecurityTests {

    @Autowired
    private SearchService searchService;

    @Test
    void tc016_sqlInjectionLikeQueryDoesNotBypassFiltering() {
        UUID teamId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        searchService.upsert(new SearchDocumentUpsertRequest(
                UUID.randomUUID(),
                "TASK",
                teamId,
                projectId,
                "Safe title",
                "Normal text"
        ));
        searchService.upsert(new SearchDocumentUpsertRequest(
                UUID.randomUUID(),
                "TASK",
                teamId,
                projectId,
                "Another safe title",
                "Second text"
        ));

        var injectedQueryResult = searchService.search("' OR 1=1 --", "TASK", teamId, projectId, PageRequest.of(0, 20));
        var normalResult = searchService.search("Safe", "TASK", teamId, projectId, PageRequest.of(0, 20));

        assertThat(injectedQueryResult.getTotalElements()).isZero();
        assertThat(normalResult.getTotalElements()).isEqualTo(2);
    }
}
