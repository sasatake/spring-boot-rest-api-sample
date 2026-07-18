# システムアーキテクチャ

## 概要

Spring Boot を用いた図書館管理システムの REST API。
書籍・著者・カテゴリ・会員を管理し、貸出・返却のライフサイクルを扱う。

## 技術スタック

| 項目 | 内容 |
|---|---|
| 言語 | Java 25 |
| フレームワーク | Spring Boot 4.0.6 |
| ビルドツール | Gradle 9.5.1 |
| ORM | MyBatis 4.0.1 |
| DB マイグレーション | Flyway |
| テスト | JUnit 5 / @WebMvcTest / @SpringBootTest(PostgreSQL 結合テスト) |
| カバレッジ | JaCoCo(PR にレポートをコメント) |
| 静的解析 | Checkstyle / PMD(警告のみ、CI は落とさない) |
| CI | GitHub Actions |
| ローカル環境 | Docker Compose |

## DB 構成

| 環境 | DB |
|---|---|
| ローカル開発 | PostgreSQL 17（Docker Compose） |
| 本番 | PostgreSQL 17 |

## ローカル起動手順

```bash
# PostgreSQL 起動
docker compose up -d

# アプリ起動
./gradlew bootRun
```

起動後、以下で API 仕様を確認できる（springdoc-openapi による自動生成）。

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## テスト実行

結合テストは `library_test` データベース(`docker compose up -d` で自動作成)に接続する。

```bash
./gradlew test
```

※ 既存の `postgres_data` ボリュームがある場合、`library_test` は作成済みでないため
`docker compose down -v && docker compose up -d` で再作成するか、手動で
`CREATE DATABASE library_test OWNER library;` を実行する。
