package com.kai_lam.searchandfiltered_service.repository;

import com.kai_lam.searchandfiltered_service.model.SearchDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID>, JpaSpecificationExecutor<SearchDocument> {
}
