CREATE TABLE book_authors (
    book_id BIGINT NOT NULL REFERENCES books (id),
    author_id BIGINT NOT NULL REFERENCES authors (id),
    PRIMARY KEY (book_id, author_id)
);
