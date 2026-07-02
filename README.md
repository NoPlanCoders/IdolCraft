# 学園アイドルマスター風プロデュースMOD (Minecraft Forge 1.20.1)

「学園アイドルマスター（学マス）」のデッキ構築・センスバフ（集中・好調・絶好調）システムを
Minecraft Forge 1.20.1 に落とし込んだMODです。

## 0. 事前調査で確認した本家仕様（実装への反映内容）

- **山札／手札／捨て札／除外の循環**：手札に来たカードは使用・未使用に関わらず基本すべて捨て札へ行く。
  「レッスン中1回」（＝Oカード）だけは捨て札ではなく除外へ行き、そのレッスン（＝デッキが有効な間）は
  二度と山札に戻らない。山札が尽きたら捨て札をシャッフルして新しい山札にする。
  → `DeckService` の `performAction` / 内部 `drawCards` に忠実に再現。
- **好調＝スコア50%アップの持続バフ。重ねがけしても倍率は伸びず、持続ターンが延長するだけ**。
  → `BuffState#addGoodCondition` は上書きではなく加算。
- **絶好調＝「好調のスコア上昇50%の値に、好調の付与ターン数×10%を加算」した倍率に変わる。
  好調が無い状態では絶好調単体では何も効果を発揮しない**（本家Wiki準拠）。
  → `ScoreMath#calculateDamage` に反映。
- **集中＝時間経過やターンでは減少せず、リセットされるまで加算され続ける永続スタック**。
  → `BuffState#tickDown` は集中を減らさない。

これらは本家Wiki（学園アイドルマスターコンテストWiki* 他）の記述を確認した上で実装しています。

## 1. 主な仕様と実装対応

| 仕様書の項目 | 実装箇所 |
|---|---|
| プロデュース手帳 | `item/HandbookItem.java` |
| 元気＝衝撃吸収(Absorption)流用、コスト消費で優先減算 | `util/GenkiHelper.java` |
| 独自バフステータス（集中/好調/絶好調/汎用パッシブ） | `buff/BuffType.java`, `buff/BuffState.java` |
| プロデューサーランク＝進捗(Advancement)連動、未達成時にキャンセル＋メッセージ | `card/CardDefinition#requiredAdvancement`, `util/AdvancementHelper.java`, `DeckService#performAction` |
| デッキ状態をCapabilityで保持 | `capability/IDeckData.java`, `DeckDataImpl.java`, `DeckDataProvider.java`, `CapabilityEvents.java` |
| Lカード/Oカードのタグ付け | `card/CardType.java` |
| 1アクションの流れ（選択→発動→捨て札/除外→残り手札捨て→3枚ドロー） | `capability/DeckService.java#performAction` |
| 山札切れ時の捨て札シャッフル再構成 | `DeckService#drawCards` |
| Rキー/就寝でデッキ完全リセット | `client/KeyBindings.java`, `event/SleepHandler.java`, `network/packet/ResetDeckPacket.java` |
| リセット時のバフ・元気の完全初期化 | `DeckService#resetDeck` |
| HUD（左：バフ一覧＋好調/絶好調秒数カウントダウン、下：手札3枚＋選択ハイライト） | `client/gui/GakumasHudOverlay.java`, `client/ClientDeckState.java` |
| 好調/絶好調をリアルタイムTickで管理（1ターン=5秒=100Tick換算） | `event/PlayerTickHandler.java`, `buff/BuffState#tickDown`, `capability/DeckService#TICKS_PER_TURN` |
| 初期6カード（うち2枚は上位進捗必須、うち1枚は初手確定） | `card/impl/ProduceCards.java` |
| 拡張性（新プラン・新バフ・新カードタイプの追加容易性） | `card/CardEffect.java`（関数型IF）, `card/CardDefinition.java`（Builder）, `card/CardRegistry.java`, `buff/BuffState`の汎用`passiveFlags`/`customCounters` |

## 2. カード一覧（初期実装6枚）

1. **アピールの基本**（L / 体力4）：ターゲットに9の魔法ダメージ（集中・好調・絶好調を反映した計算式）
2. **表現の基本**（O / コストなし）：元気+4
3. **振る舞いの基本**（L / 体力1）：元気+1、好調2ターン(10秒)
4. **表情の基本**（L / 体力1）：元気+1、集中+2
5. **静かな意志**（O / 体力4・要上位進捗・初手確定）：集中+3、好調2ターン(10秒)
6. **演出計画**（O / 体力4・要上位進捗）：絶好調3ターン(15秒)、以後リセットまでカード使用毎に元気+2の常時パッシブ

「上位進捗」は `data/gakumas_produce/advancements/rank/upper.json` にテスト用として
「ダイヤモンドを入手する」進捗を仮設定しています。実運用に合わせて自由に差し替えてください。

## 3. 操作方法

- 手帳を手に持つとHUDが表示されます。
- **スニーク＋マウスホイール**：手札の選択カードを切り替え
- **右クリック**：選択中のカードを発動
- **Gキー**：カードを発動せずスキップ（手札はすべて捨て札へ）
- **Rキー**：デッキを完全リセット（※バニラの「オフハンド入れ替え」キーとデフォルトで重複するため、
  Minecraftのコントロール設定から好みのキーに変更してください）
- ベッドで寝て朝を迎えるとデッキが自動的に完全リセットされます

## 4. 拡張の仕方（例：将来「ロジック」プランを追加する場合）

1. `buff/BuffType` に新しいバフ種別を追加（または `BuffState.customCounters` を使い
   `BuffState`自体を変更せずに新規ステータスを実装することも可能）。
2. 新カードは `CardDefinition.builder(...).effect((player, deck) -> {...}).build()` で定義し、
   `CardRegistry.register(...)` を1行呼ぶだけで登録完了。
3. ダメージ計算式を差し替えたい場合は `util/ScoreMath` を新設計に合わせて拡張、
   または `CardEffect` 内で独自の計算ロジックを直接書くことも可能（既存カードに影響しない）。

`DeckService`（デッキの循環ロジック）・`GenkiHelper`（元気管理）・`AdvancementHelper`（ランク判定）は
カードの中身を一切知らないため、新プラン追加時にこれらを変更する必要はありません。

## 5. ビルド方法・注意事項

このプロジェクトはネットワーク制限のあるサンドボックス環境で作成されたため、
**Forge本体（MDK）のダウンロード・実際のコンパイル・実機テストは行えていません。**
お手元の開発環境で以下の手順を行ってください。

1. `build.gradle` は Forge 1.20.1 (47.3.0) 用の標準的なForgeGradle構成です。
   お手持ちのForge 1.20.1 MDKのbuild.gradle/gradlewラッパーと差し替えるか、
   本プロジェクトに `gradlew`一式を追加してください。
2. `./gradlew build` でMOD jarをビルドできます。
3. アイテムのテクスチャは仮に `minecraft:item/paper` を割り当てています。
   `assets/gakumas_produce/textures/item/` に実際の画像を追加し、
   各 `models/item/*.json` の `layer0` を差し替えてください。
4. HUDは簡易的な矩形＋テキスト描画で実装しています。デザインを凝る場合は
   `client/gui/GakumasHudOverlay.java` を独自の描画に差し替えてください。
5. コード全体はMinecraft 1.20.1 / Forge 47.3.0 の公式(official)マッピングを前提に記述しています。
   実際にビルドしてエラーが出た場合は、Forgeのバージョン差異によるAPI変更の可能性があるため
   該当箇所（特に `PlayerWakeUpEvent`, `RegisterGuiOverlaysEvent`, `SimpleChannel` 周り）を
   ご利用のForgeバージョンのAPIに合わせて微調整してください。
