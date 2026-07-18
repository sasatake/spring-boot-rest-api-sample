# 実装ガイドライン

このプロジェクトで機能を実装する際の共通指針。書籍 CRUD(ロードマップ ステップ2)の実装を基準例とする。

## 開発フロー — ドキュメント駆動

1. `docs/specs/` の仕様書と `docs/openapi.yaml` を正とし、実装はそれに従う
2. 仕様にない挙動が必要になった場合は、実装より先に仕様書を更新する
3. ステップ完了時に `docs/roadmap.md` の状態を更新する

## レイヤー構成と責務

| 層 | パッケージ | 責務 |
|---|---|---|
| Controller | `controller` | HTTP の入出力のみ。パラメータの受け取り・デフォルト値、ステータスコード、`Location` ヘッダー。ロジックは持たない |
| Service | `service` | ビジネスロジックとトランザクション境界。`@Transactional` はここに付ける(参照系は `readOnly = true`) |
| Mapper | `mapper` | SQL(MyBatis)。DB アクセスはこの層に閉じる |

- `model`: DB のエンティティに対応する型
- `dto`: リクエスト・レスポンス専用の型。イミュータブルな `record` で定義する
- `exception`: 業務例外と例外ハンドラ

## バリデーション — 3層で守る

役割の異なる3つの層で入力を検証する。

1. **Bean Validation(`dto`)** — リクエスト単体で判定できる形式チェック(必須、文字数、ISBN-13 形式など)。アノテーションで宣言し、`@Valid` で Controller の入口で弾く。複数の違反を一度にまとめて返せる
2. **Service** — DB の状態や実行時刻に依存するチェック(一意性、参照先 ID の存在、当年以下の出版年など)。エラーの意味を区別する:
   - 「入力が不正」(存在しない参照先 ID など)→ `400 Bad Request`
   - 「入力は正しいが現在の状態と衝突」(一意制約違反、貸出中の削除など)→ `409 Conflict`
3. **DB 制約** — `UNIQUE` / `NOT NULL` / 外部キー。並行リクエストなどでアプリ層のチェックをすり抜けても、データ不整合だけは起こさない最後の砦

アプリ層のチェックは「クライアントが自力で修正できる親切なエラーを返すため」、DB 制約は「整合性を保証するため」と役割を分ける。エラーメッセージには修正に必要な情報を含める(例: `not found: id=999`)。

## エラーハンドリング

- Service は業務例外(`NotFoundException` / `ConflictException` / `InvalidRequestException`)を投げるだけにする
- HTTP ステータスと共通エラー形式([common.md](./specs/common.md#エラーレスポンス形式))への変換は `GlobalExceptionHandler`(`@RestControllerAdvice`)に集約する
- `errors` 配列はバリデーションエラー(400)の場合のみ含める
- DB 制約違反(`DataIntegrityViolationException`)は `409 Conflict` に変換する。並行リクエストが Service の事前チェックをすり抜けて DB 制約に落ちた場合に、500 ではなく一貫したエラーを返すための安全網

## データベース

- スキーマ変更は必ず Flyway マイグレーション(`V{連番}__{内容}.sql`)で行う。適用済みのマイグレーションファイルは変更しない
- PK は `BIGINT GENERATED ALWAYS AS IDENTITY`
- 一意制約・外部キーは DB に定義する(前述の「最後の砦」)
- **削除は履歴保持を考慮する**: 監査証跡(貸出履歴など)から参照されるエンティティは物理削除せず、`deleted_at` による論理削除とする(例: 書籍)。取得系・一意性判定は未削除のレコードのみを対象とする
- 論理削除するテーブルの一意制約は部分一意インデックス(`CREATE UNIQUE INDEX ... WHERE deleted_at IS NULL`)で定義し、削除済みレコードの値を再登録可能にする

## MyBatis

- SQL は XML マッパー(`resources/mapper/**/*.xml`)に書く。動的 SQL(`<if>` / `<foreach>`)が読みやすいため
- カラム名 → プロパティ名の変換は `map-underscore-to-camel-case` に任せ、`resultMap` は関連の組み立てなど必要な場合のみ書く
- 多対多の関連(著者・カテゴリなど)は `resultMap` の `collection` + ネスト select で取得する。N+1 が性能問題になった時点で JOIN + ページング分離に置き換える(早すぎる最適化をしない)

## テスト

- 機能の受け入れ確認は **結合テスト**で行う: `@SpringBootTest` + `MockMvc` + 実 PostgreSQL(`library_test` DB)。バリデーション → SQL → エラー変換の連携、マイグレーションとの整合まで丸ごと検証できる
- **仕様書の処理ステップをテストケースに写像する**: 正常系に加え、各エラーパス(400 / 404 / 409)と境界の振る舞い(更新時に自分の一意値はそのまま使える、など)を1つずつ押さえる
- **テストは独立させる**: `@BeforeEach` で `TRUNCATE ... RESTART IDENTITY CASCADE` し、各テストが必要なデータを自分で用意する。実行順序に依存させない
- **テストデータの作り方**: テスト対象の機能は API 経由で操作し、前提データ(未実装の機能のレコード)は `JdbcTemplate` で直接投入する。対象機能の API が実装されたら置き換える
- DB に依存しない複雑なロジックが増えたら、`@WebMvcTest` や Service の単体テストを併用する(結合テストで仕様全体、単体テストで細かい分岐)

## 静的解析・カバレッジ

- Checkstyle(`config/checkstyle/checkstyle.xml`)・PMD(`config/pmd/ruleset.xml`)・SpotBugs(`config/spotbugs/exclude.xml`)を `./gradlew check` で実行する。**警告のみ運用**(CI は落とさない)だが、新規・変更コードで警告を増やさない
- カバレッジは JaCoCo で計測し、PR にレポートがコメントされる。数値目標は設けないが、結合テストの方針(仕様の処理ステップを網羅)を守っていれば自然と維持される

## API 設計

- ページネーション・エラー形式・ステータスコードは [common.md](./specs/common.md) に従う
- 登録は `201 Created` + `Location: /{リソース}/{id}` ヘッダー、削除は `204 No Content`
- `docs/openapi.yaml` と実装を一致させる(ロードマップ ステップ7で自動生成への置き換えを予定)
