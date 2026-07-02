# Gakumas Produce

Minecraft Forge 1.20.1 向けの、プロデュースカードとバフ管理を追加する MOD です。

## 概要

この MOD は、手帳アイテムを使ってカードを選択し、カード効果でスコア計算やバフ管理を行うプロデュース風の遊びを Minecraft に追加します。

## 主な機能

- プロデュース手帳アイテム
- カード6種
  - アピールの基本
  - 表現の基本
  - 振る舞いの基本
  - 表情の基本
  - 静かな意志
  - 演出計画
- 山札、手札、捨て札、除外カードの管理
- 集中、好調、絶好調などのバフ管理
- HUD 表示
- キーバインドによるカード操作
- 睡眠時のデッキリセット
- Forge Capability によるプレイヤーデータ保存

## 対応環境

- Minecraft 1.20.1
- Minecraft Forge 47.3.0 以上
- Java 17

## 開発環境の起動

```powershell
.\gradlew.bat runClient
```

## ビルド

```powershell
.\gradlew.bat build
```

ビルドした MOD jar は `build/libs/` に出力されます。

## リポジトリ構成

```text
gradle/                 Gradle wrapper
src/main/java/          MOD の Java ソースコード
src/main/resources/     assets、lang、models、textures、mods.toml
build.gradle            ForgeGradle のビルド設定
gradle.properties       プロジェクト設定
settings.gradle         Gradle プロジェクト設定
```

## Git 管理しないもの

以下はローカルで生成されるため、Git 管理しません。

- `.gradle/`
- `build/`
- `run/`
- ローカルのテストワールド
- ビルド済み jar

テストワールドを残したい場合は、リポジトリ外にバックアップしてから作業してください。必要になったら `run/saves/` に戻して `runClient` を起動します。

## ライセンス

MIT
