ALTER TABLE members ADD COLUMN deleted_at TIMESTAMP;

-- メールアドレスは未削除の会員の中で一意（論理削除された会員のメールアドレスは再登録可能）
ALTER TABLE members DROP CONSTRAINT members_email_key;
CREATE UNIQUE INDEX uq_members_email ON members (email) WHERE deleted_at IS NULL;
