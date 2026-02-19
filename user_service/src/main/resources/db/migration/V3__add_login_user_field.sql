ALTER TABLE user_profile
    ADD COLUMN IF NOT EXISTS login_user VARCHAR(100);

UPDATE user_profile
SET login_user = name_user
WHERE login_user IS NULL;

CREATE INDEX IF NOT EXISTS idx_user_profile_login_user ON user_profile(login_user);
