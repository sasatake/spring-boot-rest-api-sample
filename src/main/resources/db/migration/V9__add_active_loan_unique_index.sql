-- 1冊の書籍は同時に1人の会員にのみ貸し出せる。
-- アプリ層の貸出中チェックを並行リクエストがすり抜けた場合の最後の砦
CREATE UNIQUE INDEX uq_loans_active_book ON loans (book_id) WHERE returned_at IS NULL;
