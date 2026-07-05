# 書籍機能 仕様書

## 概要

書籍情報の登録・参照・更新・削除を行う機能。著者・カテゴリとは多対多で関連付けられる。

## エンドポイント

### 一覧取得

```
GET /books
```

**クエリパラメータ**

| 名前 | 型 | 必須 | 説明 |
|---|---|---|---|
| title | string | 任意 | タイトル部分一致検索 |
| categoryId | long | 任意 | カテゴリで絞り込み |
| page | int | 任意 | ページ番号（デフォルト: 0） |
| size | int | 任意 | 1ページ件数（デフォルト: 20） |

**レスポンス例（200 OK）**

```json
{
  "content": [
    {
      "id": 1,
      "title": "吾輩は猫である",
      "isbn": "978-4-10-101035-9",
      "publishedYear": 1905,
      "authors": [{ "id": 1, "name": "夏目漱石" }],
      "categories": [{ "id": 1, "name": "小説" }]
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 詳細取得

```
GET /books/{id}
```

**レスポンス例（200 OK）**

```json
{
  "id": 1,
  "title": "吾輩は猫である",
  "isbn": "978-4-10-101035-9",
  "publishedYear": 1905,
  "description": "猫の視点から人間社会を風刺した小説",
  "authors": [{ "id": 1, "name": "夏目漱石" }],
  "categories": [{ "id": 1, "name": "小説" }]
}
```

**エラーケース**

| ステータス | 条件 |
|---|---|
| 404 Not Found | 指定した id の書籍が存在しない |

### 登録

```
POST /books
```

**リクエストボディ**

```json
{
  "title": "吾輩は猫である",
  "isbn": "978-4-10-101035-9",
  "publishedYear": 1905,
  "description": "猫の視点から人間社会を風刺した小説",
  "authorIds": [1],
  "categoryIds": [1]
}
```

**バリデーションルール**

| 項目 | ルール |
|---|---|
| title | 必須、1〜255文字 |
| isbn | 必須、ISBN-13形式、一意 |
| publishedYear | 任意、1000〜当年以下 |
| authorIds | 必須、1件以上、存在する著者ID |
| categoryIds | 任意、存在するカテゴリID |

**レスポンス**

| ステータス | 条件 |
|---|---|
| 201 Created | 登録成功。Location ヘッダーに `/books/{id}` |
| 400 Bad Request | バリデーションエラー |
| 409 Conflict | isbn が既に登録済み |

### 更新

```
PUT /books/{id}
```

- リクエストボディは登録時と同じ形式
- バリデーションルールも登録時と同じ

**レスポンス**

| ステータス | 条件 |
|---|---|
| 200 OK | 更新成功 |
| 400 Bad Request | バリデーションエラー |
| 404 Not Found | 指定した id の書籍が存在しない |
| 409 Conflict | isbn が他の書籍と重複 |

### 削除

```
DELETE /books/{id}
```

**レスポンス**

| ステータス | 条件 |
|---|---|
| 204 No Content | 削除成功 |
| 404 Not Found | 指定した id の書籍が存在しない |
| 409 Conflict | 貸出中（returned_at が null の loan が存在）の場合は削除不可 |

## ビジネスルール

- ISBN は一意であること
- 貸出中の書籍は削除できない
- 著者は最低1名以上関連付ける必要がある
- カテゴリは0件でも登録可能
