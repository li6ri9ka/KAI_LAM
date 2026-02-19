CREATE TABLE info_project (
    id_info_project UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    name_project VARCHAR(200) NOT NULL,
    project_description VARCHAR(2000) NULL,
    github_link_project VARCHAR(500) NULL
);

CREATE TABLE user_project (
    id_user_project UUID PRIMARY KEY,
    info_project_id UUID NOT NULL,
    user_team_id UUID NOT NULL,
    project_role VARCHAR(100) NOT NULL,
    CONSTRAINT fk_user_project_info_project FOREIGN KEY (info_project_id) REFERENCES info_project (id_info_project) ON DELETE CASCADE,
    CONSTRAINT uk_user_project UNIQUE (info_project_id, user_team_id)
);

CREATE TABLE repository (
    id_repository UUID PRIMARY KEY,
    info_project_id UUID NOT NULL,
    provider VARCHAR(100) NULL,
    repo_url VARCHAR(500) NULL,
    readme_md TEXT NULL,
    CONSTRAINT fk_repository_info_project FOREIGN KEY (info_project_id) REFERENCES info_project (id_info_project) ON DELETE CASCADE
);

CREATE TABLE build (
    id_build UUID PRIMARY KEY,
    name_build VARCHAR(200) NOT NULL,
    release_version VARCHAR(100) NULL,
    info_project_id UUID NOT NULL,
    CONSTRAINT fk_build_info_project FOREIGN KEY (info_project_id) REFERENCES info_project (id_info_project) ON DELETE CASCADE
);

CREATE TABLE file_build (
    id_file_build UUID PRIMARY KEY,
    build_id UUID NOT NULL,
    link_file_build_s3 VARCHAR(500) NOT NULL,
    CONSTRAINT fk_file_build_build FOREIGN KEY (build_id) REFERENCES build (id_build) ON DELETE CASCADE
);

CREATE TABLE meeting_transcrib (
    id_meeting_transcrib UUID PRIMARY KEY,
    file_transcrib_meet_s3 VARCHAR(500) NULL,
    short_description_meet VARCHAR(1000) NULL,
    info_project_id UUID NOT NULL,
    CONSTRAINT fk_meeting_transcrib_info_project FOREIGN KEY (info_project_id) REFERENCES info_project (id_info_project) ON DELETE CASCADE
);

CREATE TABLE file_meeting (
    id_file_meeting UUID PRIMARY KEY,
    file_meet_s3 VARCHAR(500) NOT NULL,
    meeting_transcrib_id UUID NOT NULL,
    CONSTRAINT fk_file_meeting_meeting_transcrib FOREIGN KEY (meeting_transcrib_id) REFERENCES meeting_transcrib (id_meeting_transcrib) ON DELETE CASCADE
);

CREATE TABLE secret_keys (
    id_secret_key UUID PRIMARY KEY,
    name_secret_key VARCHAR(200) NOT NULL,
    encrypted_value TEXT NOT NULL
);

CREATE TABLE key_project (
    id_key_project UUID PRIMARY KEY,
    secret_key_id UUID NOT NULL,
    info_project_id UUID NOT NULL,
    CONSTRAINT fk_key_project_secret_key FOREIGN KEY (secret_key_id) REFERENCES secret_keys (id_secret_key) ON DELETE CASCADE,
    CONSTRAINT fk_key_project_info_project FOREIGN KEY (info_project_id) REFERENCES info_project (id_info_project) ON DELETE CASCADE,
    CONSTRAINT uk_key_project UNIQUE (secret_key_id, info_project_id)
);

CREATE INDEX idx_info_project_team_id ON info_project(team_id);
CREATE INDEX idx_user_project_info_project_id ON user_project(info_project_id);
CREATE INDEX idx_user_project_user_team_id ON user_project(user_team_id);
CREATE INDEX idx_repository_info_project_id ON repository(info_project_id);
CREATE INDEX idx_build_info_project_id ON build(info_project_id);
CREATE INDEX idx_file_build_build_id ON file_build(build_id);
CREATE INDEX idx_meeting_transcrib_info_project_id ON meeting_transcrib(info_project_id);
CREATE INDEX idx_file_meeting_meeting_transcrib_id ON file_meeting(meeting_transcrib_id);
CREATE INDEX idx_key_project_info_project_id ON key_project(info_project_id);
CREATE INDEX idx_key_project_secret_key_id ON key_project(secret_key_id);
