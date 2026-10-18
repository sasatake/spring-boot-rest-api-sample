# システムアーキテクチャ

## 概要

Spring Boot を用いた図書館管理システムの REST API。
書籍・著者・カテゴリ・会員を管理し、貸出・返却のライフサイクルを扱う。

## 技術スタック

| 項目 | 内容 |
|---|---|
| 言語 | Java 25 |
| フレームワーク | Spring Boot 4.0.6 |
| ビルドツール | Gradle 9.8.1 |
| ORM | MyBatis 4.1.0 |
| DB マイグレーション | Flyway |
| API 仕様 | springdoc-openapi 3.1.1(Swagger UI 自動生成) |
| テスト | JUnit 5 / @WebMvcTest / @SpringBootTest(PostgreSQL 結合テスト) |
| カバレッジ | JaCoCo(PR にレポートをコメント) |
| 静的解析 | Checkstyle / PMD / SpotBugs(警告のみ、CI は落とさない) |
| CI | GitHub Actions |
| ローカル環境 | Docker Compose |

## DB 構成

| 環境 | DB |
|---|---|
| ローカル開発 | PostgreSQL 17（Docker Compose） |
| 本番 | PostgreSQL 17 |

## 開発コマンド

起動・テスト・静的解析のコマンドは [README.md](../README.md) にまとめてある。Dev Container での開発は [devcontainer.md](./devcontainer.md) を参照。
