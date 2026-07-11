CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(17) NOT NULL,
    published_year INT,
    description TEXT,
    deleted_at TIMESTAMP
);

-- ISBN は未削除の書籍の中で一意（論理削除された書籍の ISBN は再登録可能）
CREATE UNIQUE INDEX uq_books_isbn ON books (isbn) WHERE deleted_at IS NULL;
