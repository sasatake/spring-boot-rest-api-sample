# Dev Container での開発(Zed)

このリポジトリは Dev Container で開発・実行できる。エディタは [Zed](https://zed.dev) を前提とする。

## 前提条件

- Docker(Docker Desktop / OrbStack など)が起動していて `docker` が PATH にあること
- Zed(Dev Container 対応版)

## 開き方

1. Zed でこのリポジトリを開く
2. `.devcontainer/devcontainer.json` が検出され、コンテナで開くかのプロンプトが表示されるので「Open in Container」を選ぶ
3. プロンプトを閉じてしまった場合はコマンドパレットから `Project: Open Remote` → 「Connect Dev Container」で開ける

初回はイメージの取得と Gradle Wrapper のダウンロード(`postCreateCommand`)が走るため数分かかる。

接続後は Zed のターミナル・タスク・言語サーバー(Java 拡張 / JDTLS)がコンテナ内で動作する。

## 構成

- [`../.devcontainer/devcontainer.json`](../.devcontainer/devcontainer.json) — Dev Container 本体の定義
- [`../.devcontainer/compose.yml`](../.devcontainer/compose.yml) — ルートの [`docker-compose.yml`](../docker-compose.yml) に開発用 `app` サービスを重ねるオーバーレイ

`app` サービス(JDK 25 入り)は `network_mode: service:postgres` で PostgreSQL コンテナとネットワーク名前空間を共有している。これにより、アプリ([`application.properties`](../src/main/resources/application.properties))もテスト([`application-test.properties`](../src/test/resources/application-test.properties))も設定変更なしで `localhost:5432` に接続できる。

ホスト側へは以下のポートが公開される。

| ポート | 用途 |
| --- | --- |
| 5432 | PostgreSQL(`library` / `library_test`) |
| 8080 | Spring Boot アプリ |

Gradle のキャッシュ(`/home/vscode/.gradle`)は名前付きボリューム `gradle-cache` に永続化され、コンテナを作り直しても依存関係の再ダウンロードは発生しない。

## コンテナ内での操作

```sh
# テスト実行
./gradlew test

# アプリ起動(http://localhost:8080 でホストからもアクセス可能)
./gradlew bootRun
```

## 注意事項

- Zed は `devcontainer.json` の変更を自動で反映しない。設定を変えたら `docker compose -f docker-compose.yml -f .devcontainer/compose.yml down` などでコンテナを止めてから開き直すこと
- ホスト側で `docker compose up` による PostgreSQL を別途起動している場合、5432 ポートが衝突するので先に止めておくこと
