# spring-boot-rest-api-sample

Spring Boot による図書館管理 REST API のサンプル実装。書籍・著者・カテゴリ・会員を管理し、貸出・返却のライフサイクルを扱う。

ドキュメント駆動で開発しており、仕様は `docs/` 配下に置いている。

## 技術スタック

| 項目 | 内容 |
|---|---|
| 言語 / ビルド | Java 25 / Gradle 9.5.1（Wrapper 同梱） |
| フレームワーク | Spring Boot 4.0.6 |
| ORM | MyBatis 4.0.1（XML マッパー） |
| DB | PostgreSQL 17 / Flyway マイグレーション |
| API 仕様 | springdoc-openapi 3.0.3（Swagger UI 自動生成） |
| テスト | JUnit 5 / `@WebMvcTest` / `@SpringBootTest`（PostgreSQL 結合テスト） |
| カバレッジ | JaCoCo（PR にレポートをコメント） |
| 静的解析 | Checkstyle / PMD / SpotBugs（警告のみ。CI は落とさない） |
| CI | GitHub Actions |

詳細は [docs/architecture.md](docs/architecture.md)。

## クイックスタート

必要なもの: Docker（Compose）、JDK 25。

```bash
# PostgreSQL 起動（library / library_test の両 DB が作られる）
docker compose up -d

# アプリ起動
./gradlew bootRun
```

起動したら:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Flyway が起動時にマイグレーションを適用するので、DB の初期化作業は不要。

### Dev Container で開く

ホストに JDK を入れずに開発できる。Zed / VS Code のどちらにも対応（`remoteUser` は `vscode`）。

手順は [docs/devcontainer.md](docs/devcontainer.md)。

## API

| メソッド | パス | 説明 |
|---|---|---|
| GET | `/books` | 書籍一覧（ページング） |
| GET | `/books/{id}` | 書籍取得 |
| POST | `/books` | 書籍登録 |
| PUT | `/books/{id}` | 書籍更新 |
| DELETE | `/books/{id}` | 書籍削除（論理削除） |
| GET | `/authors` | 著者一覧（ページング） |
| GET | `/authors/{id}` | 著者取得 |
| POST | `/authors` | 著者登録 |
| PUT | `/authors/{id}` | 著者更新 |
| DELETE | `/authors/{id}` | 著者削除 |
| GET | `/categories` | カテゴリ一覧 |
| POST | `/categories` | カテゴリ登録 |
| DELETE | `/categories/{id}` | カテゴリ削除 |
| GET | `/members` | 会員一覧（ページング） |
| GET | `/members/{id}` | 会員取得 |
| POST | `/members` | 会員登録 |
| PUT | `/members/{id}` | 会員更新 |
| DELETE | `/members/{id}` | 会員削除（論理削除） |
| GET | `/loans` | 貸出一覧（ページング） |
| GET | `/loans/overdue` | 延滞中の貸出一覧 |
| POST | `/loans` | 貸出 |
| PATCH | `/loans/{id}/return` | 返却 |

エンドポイントごとの仕様は [docs/specs/](docs/specs/)、スキーマ定義は [docs/openapi.yaml](docs/openapi.yaml) にある。

## テスト

結合テストは `library_test` データベースに接続する（`docker compose up -d` 時に自動作成）。

```bash
./gradlew test          # テスト実行 + JaCoCo レポート生成
```

レポートは `build/reports/tests/test/index.html`、カバレッジは `build/reports/jacoco/test/html/index.html`。

> 既存の `postgres_data` ボリュームが残っている場合、`library_test` は作成されない。
> `docker compose down -v && docker compose up -d` で作り直すか、手動で
> `CREATE DATABASE library_test OWNER library;` を実行する。

## 静的解析

いずれも警告のみの運用で、違反があってもビルドは失敗しない。

```bash
./gradlew checkstyleMain checkstyleTest pmdMain pmdTest spotbugsMain spotbugsTest
```

レポートは `build/reports/{checkstyle,pmd,spotbugs}/` に出力される。設定は `config/` 配下。

## プロジェクト構成

```
src/main/java/org/sample/spring/rest/api/
├── controller/   REST エンドポイント（入力の受け取りとレスポンス変換）
├── service/      ビジネスロジック・トランザクション境界
├── mapper/       MyBatis マッパーインタフェース
├── model/        エンティティ
├── dto/          リクエスト / レスポンス DTO
├── exception/    例外と GlobalExceptionHandler
└── config/       OpenAPI 設定

src/main/resources/
├── db/migration/ Flyway マイグレーション（V1〜V9）
└── mapper/       MyBatis XML マッパー
```

レイヤーの責務分担やバリデーション方針は [docs/guidelines.md](docs/guidelines.md) を参照。

## ドキュメント

| ファイル | 内容 |
|---|---|
| [docs/architecture.md](docs/architecture.md) | システム構成・技術選定 |
| [docs/database.md](docs/database.md) | ER 図・テーブル定義 |
| [docs/guidelines.md](docs/guidelines.md) | 実装ガイドライン（レイヤー責務・バリデーション・テスト方針） |
| [docs/specs/](docs/specs/) | エンドポイント単位の API 仕様 |
| [docs/openapi.yaml](docs/openapi.yaml) | OpenAPI 定義 |
| [docs/devcontainer.md](docs/devcontainer.md) | Dev Container での開発手順 |
| [docs/roadmap.md](docs/roadmap.md) | 開発ロードマップと進捗 |

## CI

- **CI**（`.github/workflows/ci.yml`）— `main` への push / PR で、PostgreSQL サービスコンテナを立ててテストと静的解析を実行。PR にはカバレッジとテスト結果がコメントされ、解析レポートは artifact として保存される。
- **依存更新**（`.github/workflows/renovate.yml`）— 毎週月曜 07:00 JST に Renovate を self-hosted（Actions 上で CLI 実行）で走らせ、更新があれば PR を作成する。対象は Gradle の依存・プラグイン・Wrapper（静的解析ツールの `toolVersion` を含む）、GitHub Actions、Docker イメージ。更新方針は [renovate.json](renovate.json)、実行時の設定は `.github/renovate-global.js`。認証は自前 GitHub App のインストールトークン（`RENOVATE_APP_ID` / `RENOVATE_APP_PRIVATE_KEY`）で、個人の PAT は使わない。
- **イメージ公開**（`.github/workflows/publish.yml`）— `publish` ブランチへの push で `bootBuildImage` を実行し、`ghcr.io` にコンテナイメージを push する。
