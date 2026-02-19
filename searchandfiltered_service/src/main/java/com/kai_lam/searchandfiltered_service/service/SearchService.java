package com.kai_lam.searchandfiltered_service.service;

import com.kai_lam.searchandfiltered_service.dto.SearchDocumentResponse;
import com.kai_lam.searchandfiltered_service.dto.SearchDocumentUpsertRequest;
import com.kai_lam.searchandfiltered_service.exception.NotFoundException;
import com.kai_lam.searchandfiltered_service.model.SearchDocument;
import com.kai_lam.searchandfiltered_service.repository.SearchDocumentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SearchService {
    private final SearchDocumentRepository repository;

    public SearchService(SearchDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<SearchDocumentResponse> search(String q,
                                               String docType,
                                               UUID teamId,
                                               UUID projectId,
                                               Pageable pageable) {
        Specification<SearchDocument> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (docType != null && !docType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("docType")), docType.trim().toLowerCase()));
            }
            if (teamId != null) {
                predicates.add(cb.equal(root.get("teamId"), teamId));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.get("projectId"), projectId));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(cb.coalesce(root.get("title"), "")), pattern),
                                cb.like(cb.lower(cb.coalesce(root.get("body"), "")), pattern)
                        )
                );
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return repository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SearchDocumentResponse getById(UUID docId) {
        SearchDocument doc = repository.findById(docId).orElseThrow(() -> new NotFoundException("Document not found"));
        return toResponse(doc);
    }

    @Transactional
    public SearchDocumentResponse upsert(SearchDocumentUpsertRequest request) {
        SearchDocument doc = repository.findById(request.docId()).orElseGet(SearchDocument::new);
        doc.setDocId(request.docId());
        doc.setDocType(request.docType().trim().toUpperCase());
        doc.setTeamId(request.teamId());
        doc.setProjectId(request.projectId());
        doc.setTitle(request.title());
        doc.setBody(request.body());
        doc.setUpdatedAt(Instant.now());
        return toResponse(repository.save(doc));
    }

    @Transactional
    public void delete(UUID docId) {
        repository.deleteById(docId);
    }

    @Transactional
    public void upsertFromEvent(String docType,
                                String sourceId,
                                UUID teamId,
                                UUID projectId,
                                String title,
                                String body,
                                Instant updatedAt) {
        UUID docId = namespacedDocId(docType, sourceId);
        SearchDocument doc = repository.findById(docId).orElseGet(SearchDocument::new);
        doc.setDocId(docId);
        doc.setDocType(docType.toUpperCase());
        doc.setTeamId(teamId);
        doc.setProjectId(projectId);
        doc.setTitle(title);
        doc.setBody(body);
        doc.setUpdatedAt(updatedAt == null ? Instant.now() : updatedAt);
        repository.save(doc);
    }

    @Transactional
    public void deleteFromEvent(String docType, String sourceId) {
        repository.deleteById(namespacedDocId(docType, sourceId));
    }

    private UUID namespacedDocId(String docType, String sourceId) {
        return UUID.nameUUIDFromBytes((docType + ":" + sourceId).getBytes(StandardCharsets.UTF_8));
    }

    private SearchDocumentResponse toResponse(SearchDocument doc) {
        return new SearchDocumentResponse(
                doc.getDocId(),
                doc.getDocType(),
                doc.getTeamId(),
                doc.getProjectId(),
                doc.getTitle(),
                doc.getBody(),
                doc.getUpdatedAt()
        );
    }
}
