CREATE TABLE user_daily_view (
    id_user_daily_view UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    date DATE NOT NULL,
    today_task_ids JSONB NOT NULL,
    overdue_task_ids JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_user_daily_view UNIQUE (user_id, date)
);

CREATE INDEX idx_user_daily_view_user_id ON user_daily_view(user_id);
CREATE INDEX idx_user_daily_view_date ON user_daily_view(date);
CREATE INDEX idx_user_daily_view_updated_at ON user_daily_view(updated_at);
