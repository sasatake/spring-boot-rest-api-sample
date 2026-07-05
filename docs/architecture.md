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
| DB マイグレーション | Flyway |
| テスト | JUnit 5 / @WebMvcTest |
| CI | GitHub Actions |

## DB 構成

| 環境 | DB |
|---|---|
| 開発 | H2（インメモリ） |
| 本番 | PostgreSQL |
