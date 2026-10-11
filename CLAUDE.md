# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

図書館管理の REST API サンプル。Spring Boot / MyBatis / Flyway / PostgreSQL。

## 最初に読むもの

実装規約は [docs/guidelines.md](docs/guidelines.md) が正。**レイヤーの責務とトランザクション境界、3 層バリデーション（400 と 409 の使い分け）、エラー変換の集約先、MyBatis と論理削除の規約、テストの書き方**はすべてそこにある。コードを書く前に読むこと。

| 知りたいこと | 参照先 |
|---|---|
| 実装規約・設計判断の理由 | [docs/guidelines.md](docs/guidelines.md) |
| 起動・テスト・静的解析のコマンド | [README.md](README.md) |
| 構成と技術選定 | [docs/architecture.md](docs/architecture.md) |
| テーブル定義・ER 図 | [docs/database.md](docs/database.md) |
| エンドポイント単位の API 仕様 | [docs/specs/](docs/specs/) |
| 共通規約（ページング・エラー形式・ステータス） | [docs/specs/common.md](docs/specs/common.md) |
| Dev Container | [docs/devcontainer.md](docs/devcontainer.md) |

## 変更するときの約束

- **仕様が実装より先。** `docs/specs/` と `docs/openapi.yaml` が正で、仕様にない挙動が必要になったら実装より先に仕様書を更新する。ステップ完了時に `docs/roadmap.md` の状態を更新する
- 適用済みの Flyway マイグレーションは書き換えない。常に新しい `V{連番}__{内容}.sql` を足す
- 静的解析は警告のみ運用で CI も落とさないが、新規・変更コードで警告を増やさない
- **ドキュメントにバージョン番号を書いてよいのは [docs/architecture.md](docs/architecture.md) の技術スタック表だけ。** 他の箇所は技術名のみにする。依存を上げたらその表を更新する

## コマンドの補足

基本は [README.md](README.md) にある。ここには、そちらに書いていないものだけ記す。

```sh
# テストと静的解析をまとめて実行
./gradlew check

# 単一クラス / 単一メソッド
./gradlew test --tests '*BookControllerIntegrationTest'
./gradlew test --tests '*BookControllerIntegrationTest.createBookWithDuplicateIsbnReturnsConflict'
```

**`./gradlew` が Java ランタイム不在で落ちる場合**、そのホストに JDK が入っていない。このリポジトリの正式な開発環境は Dev Container なので、ホストからはコンテナ越しに実行する。

```sh
docker compose -f docker-compose.yml -f .devcontainer/compose.yml exec -u vscode app ./gradlew test
```

## 知っておくと迷わないこと

- `settings.gradle` の `rootProject.name` は `demo`（Spring Initializr のまま）。成果物名が `demo-*.jar` になる
- `GreetingController` は `controller` ではなく **`service` パッケージ**にある（動作確認用の名残）
- 依存更新は self-hosted Renovate が毎週自動で PR を作る。バージョンを手で上げる前に [renovate.json](renovate.json) のグループ設定を確認する
