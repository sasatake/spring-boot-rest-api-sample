CREATE TABLE book_categories (
    book_id BIGINT NOT NULL REFERENCES books (id),
    category_id BIGINT NOT NULL REFERENCES categories (id),
    PRIMARY KEY (book_id, category_id)
);
