package com.kai_lam.searchandfiltered_service.controller;

import com.kai_lam.searchandfiltered_service.dto.SearchDocumentResponse;
import com.kai_lam.searchandfiltered_service.dto.SearchDocumentUpsertRequest;
import com.kai_lam.searchandfiltered_service.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/documents")
    public Page<SearchDocumentResponse> search(@RequestParam(required = false) String q,
                                               @RequestParam(required = false) String docType,
                                               @RequestParam(required = false) UUID teamId,
                                               @RequestParam(required = false) UUID projectId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(defaultValue = "updatedAt") String sortBy,
                                               @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return searchService.search(q, docType, teamId, projectId, pageable);
    }

    @GetMapping("/documents/{docId}")
    public SearchDocumentResponse get(@PathVariable UUID docId) {
        return searchService.getById(docId);
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public SearchDocumentResponse upsert(@Valid @RequestBody SearchDocumentUpsertRequest request) {
        return searchService.upsert(request);
    }

    @DeleteMapping("/documents/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID docId) {
        searchService.delete(docId);
    }
}
