-- Allow failed login attempts for unknown usernames (user_id stays null; attempted_username is stored).
-- Run once if Hibernate ddl-auto=update does not alter the column automatically.
ALTER TABLE login_history MODIFY COLUMN user_id BIGINT NULL;
