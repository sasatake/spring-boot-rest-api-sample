// Renovate の self-hosted(管理者)設定。
// 依存の更新方針そのものはリポジトリ直下の renovate.json 側に書く。
module.exports = {
  platform: 'github',

  // 自前運用なので autodiscover は使わず、対象を明示する
  repositories: ['sasatake/spring-boot-rest-api-sample'],

  // renovate.json が既にあるのでオンボーディング PR は不要。
  // requireConfig: 設定ファイルが無いリポジトリでは何もしない
  onboarding: false,
  requireConfig: 'required',

  // gradle-wrapper.properties の distributionUrl 更新だけなら不要。
  // ./gradlew wrapper を実行して wrapper 本体(jar / スクリプト)まで
  // 追従させたい場合にコメントを外す。コンテナ内に JDK の用意が必要。
  // allowedUnsafeExecutions: ['gradleWrapper'],
};
