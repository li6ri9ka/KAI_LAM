CREATE TABLE name_specialty (
    id_name_specialty UUID PRIMARY KEY,
    name_specialty VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE team (
    id_team UUID PRIMARY KEY,
    name_team VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE user_profile (
    id_user UUID PRIMARY KEY,
    name_user VARCHAR(120) NOT NULL,
    midle_name_user VARCHAR(120) NULL,
    name_spec_user_id UUID NULL,
    CONSTRAINT fk_user_profile_specialty FOREIGN KEY (name_spec_user_id) REFERENCES name_specialty (id_name_specialty)
);

CREATE TABLE user_team (
    id_user_team UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    team_id UUID NOT NULL,
    CONSTRAINT fk_user_team_user FOREIGN KEY (user_id) REFERENCES user_profile (id_user) ON DELETE CASCADE,
    CONSTRAINT fk_user_team_team FOREIGN KEY (team_id) REFERENCES team (id_team) ON DELETE CASCADE,
    CONSTRAINT uk_user_team UNIQUE (user_id, team_id)
);

CREATE INDEX idx_user_profile_name_spec_user_id ON user_profile(name_spec_user_id);
CREATE INDEX idx_user_team_user_id ON user_team(user_id);
CREATE INDEX idx_user_team_team_id ON user_team(team_id);
