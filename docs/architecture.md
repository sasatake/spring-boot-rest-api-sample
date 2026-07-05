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
| テスト | JUnit 5 / @WebMvcTest |
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
