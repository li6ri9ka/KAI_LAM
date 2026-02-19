CREATE TABLE role_user (
    id_role UUID PRIMARY KEY,
    name_role VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE auth_user (
    id_user UUID PRIMARY KEY,
    login_user VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    last_login_at TIMESTAMPTZ NULL,
    role_user_id UUID NOT NULL,
    CONSTRAINT fk_auth_user_role FOREIGN KEY (role_user_id) REFERENCES role_user (id_role)
);

CREATE TABLE refresh_session (
    id_session UUID PRIMARY KEY,
    refresh_token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_refresh_session_user FOREIGN KEY (user_id) REFERENCES auth_user (id_user)
);

CREATE INDEX idx_refresh_session_user_id ON refresh_session(user_id);
CREATE INDEX idx_refresh_session_expires_at ON refresh_session(expires_at);
