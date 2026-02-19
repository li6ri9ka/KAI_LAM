CREATE TABLE search_documents (
    doc_id UUID PRIMARY KEY,
    doc_type VARCHAR(50) NOT NULL,
    team_id UUID NULL,
    project_id UUID NULL,
    title TEXT NULL,
    body TEXT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_search_documents_doc_type ON search_documents(doc_type);
CREATE INDEX idx_search_documents_team_id ON search_documents(team_id);
CREATE INDEX idx_search_documents_project_id ON search_documents(project_id);
CREATE INDEX idx_search_documents_updated_at ON search_documents(updated_at);
