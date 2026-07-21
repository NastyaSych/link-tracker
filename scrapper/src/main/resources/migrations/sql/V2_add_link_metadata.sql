ALTER TABLE links ADD COLUMN IF NOT EXISTS last_update_title TEXT;
ALTER TABLE links ADD COLUMN IF NOT EXISTS last_update_user_name TEXT;
ALTER TABLE links ADD COLUMN IF NOT EXISTS last_update_preview TEXT;
ALTER TABLE links ADD COLUMN IF NOT EXISTS last_update_event_type TEXT;