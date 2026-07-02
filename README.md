# Gakumas Produce

Minecraft Forge 1.20.1 向けの、学マス風プロデュースカード / バフシステム MOD です。

## Features

- プロデュース手帳アイテム
- カード6種
  - アピールの基本
  - 表現の基本
  - 振る舞いの基本
  - 表情の基本
  - 静かな意志
  - 演出計画
- 山札 / 手札 / 捨て札 / 除外のデッキ管理
- 集中 / 好調 / 絶好調などのバフ管理
- HUD 表示、キーバインド、睡眠時リセット
- Forge Capability によるプレイヤーデータ保存

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.3.0 以上
- Java 17

## Development

初回セットアップ後、以下で開発用クライアントを起動できます。

```powershell
.\gradlew.bat runClient
```

ビルドは以下です。

```powershell
.\gradlew.bat build
```

生成された MOD jar は `build/libs/` に出力されます。`build/`、`.gradle/`、`run/` は生成物なので Git 管理しません。

## Repository Layout

```text
gradle/                 Gradle wrapper files
src/main/java/          Mod source code
src/main/resources/     assets, lang files, models, textures, mods.toml
build.gradle            ForgeGradle build definition
gradle.properties       project properties
settings.gradle         Gradle project settings
```

## Local Test Worlds

開発中に作成したテストワールドはリポジトリ外へ退避しています。

```text
C:\Users\mastK\Downloads\gakumas_produce_mod\test_worlds
```

必要な場合は、該当ワールドフォルダを `run/saves/` に戻してから `runClient` を起動してください。

## License

MIT
