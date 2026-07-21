CREATE TABLE IF NOT EXISTS users (
                                     user_id      BIGINT NOT NULL,
                                     tg_chat_id   BIGINT NOT NULL,
                                     PRIMARY KEY (user_id, tg_chat_id)
);

CREATE TABLE IF NOT EXISTS links (
                                     id         BIGSERIAL PRIMARY KEY,
                                     url        TEXT NOT NULL UNIQUE,
                                     last_updated_at TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS user_links (
                                          user_id BIGINT NOT NULL,
                                          tg_chat_id BIGINT NOT NULL,
                                          link_id BIGINT NOT NULL REFERENCES links(id) ON DELETE CASCADE,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (user_id, tg_chat_id, link_id),
    FOREIGN KEY (user_id, tg_chat_id) REFERENCES users(user_id, tg_chat_id) ON DELETE CASCADE
    );

-- для нахождения всех ссылок юзера в конкретном чате(/list)
CREATE INDEX idx_user_links_user_chat ON user_links(user_id, tg_chat_id);
-- для отправки оповещения об обновлении пользователю в конкретный чат
CREATE INDEX idx_user_links_link_id ON user_links(link_id);
CREATE INDEX IF NOT EXISTS idx_links_url ON links(url); -- для обновления ссылки


