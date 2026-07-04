# API エンドポイント一覧

## 書籍

| メソッド | パス | 説明 |
|---|---|---|
| GET | /books | 一覧取得 |
| GET | /books/{id} | 詳細取得 |
| POST | /books | 登録 |
| PUT | /books/{id} | 更新 |
| DELETE | /books/{id} | 削除 |

## 著者

| メソッド | パス | 説明 |
|---|---|---|
| GET | /authors | 一覧取得 |
| GET | /authors/{id} | 詳細取得 |
| POST | /authors | 登録 |
| PUT | /authors/{id} | 更新 |
| DELETE | /authors/{id} | 削除 |

## カテゴリ

| メソッド | パス | 説明 |
|---|---|---|
| GET | /categories | 一覧取得 |
| POST | /categories | 登録 |
| DELETE | /categories/{id} | 削除 |

## 会員

| メソッド | パス | 説明 |
|---|---|---|
| GET | /members | 一覧取得 |
| GET | /members/{id} | 詳細取得 |
| POST | /members | 登録 |
| PUT | /members/{id} | 更新 |
| DELETE | /members/{id} | 削除 |

## 貸出

| メソッド | パス | 説明 |
|---|---|---|
| POST | /loans | 貸出開始 |
| PATCH | /loans/{id}/return | 返却 |
| GET | /loans | 貸出一覧（フィルタ可） |
| GET | /loans/overdue | 延滞一覧 |
