CREATE TABLE task (
    id_task UUID PRIMARY KEY,
    info_project_id UUID NOT NULL,
    name_task VARCHAR(200) NOT NULL,
    description_task TEXT NULL,
    create_task TIMESTAMPTZ NOT NULL,
    estimation DOUBLE PRECISION NULL,
    status VARCHAR(30) NOT NULL,
    due_date DATE NULL,
    required_specialty_id UUID NULL
);

CREATE TABLE user_task (
    id_user_task UUID PRIMARY KEY,
    user_team_id UUID NOT NULL,
    task_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    released_at TIMESTAMPTZ NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT fk_user_task_task FOREIGN KEY (task_id) REFERENCES task (id_task) ON DELETE CASCADE
);

CREATE INDEX idx_task_info_project_id ON task(info_project_id);
CREATE INDEX idx_task_required_specialty_id ON task(required_specialty_id);
CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_due_date ON task(due_date);
CREATE INDEX idx_user_task_user_team_id ON user_task(user_team_id);
CREATE INDEX idx_user_task_task_id ON user_task(task_id);
CREATE INDEX idx_user_task_active ON user_task(active);
