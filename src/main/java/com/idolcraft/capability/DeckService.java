package com.idolcraft.capability;

import com.idolcraft.card.CardDefinition;
import com.idolcraft.card.CardRegistry;
import com.idolcraft.card.CardType;
import com.idolcraft.util.AdvancementHelper;
import com.idolcraft.util.GenkiHelper;
import com.idolcraft.util.ScoreMath;
import com.idolcraft.util.TargetingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * デッキ運用の中核ロジック。
 * 山札/手札/捨て札/除外の循環、カード発動、リセット処理をすべてここに集約している。
 * カードごとの個別効果は {@link CardDefinition#getEffect()} に委譲されるため、
 * このクラス自体はカードの内容を一切知らない（新カード追加時にここを変更する必要はない）。
 */
public final class DeckService {

    public static final int HAND_SIZE = 3;
    /** 1ターン = 5秒 = 100 Tick（20 Tick/秒） */
    public static final int TICKS_PER_TURN = 100;

    private DeckService() {}

    /**
     * デッキの完全リセット処理（仕様3）。
     * 山札・捨て札・手札・除外の全カードを山札に戻してシャッフルし、初期手札3枚を引く。
     * バフと元気もすべて0にリセットする。
     */
    public static void resetDeck(ServerPlayer player, IDeckData deck) {
        List<ResourceLocation> all = new ArrayList<>();
        // マスターカードリストが未設定(初回)の場合は、習得済み（入手済み）カードをデッキ構成にする。
        // 何も習得していなければ空デッキになる（まずカードを入手・習得する必要がある）。
        if (deck.getMasterCardList().isEmpty()) {
            deck.setMasterCardList(new ArrayList<>(deck.getOwnedCards()));
        }
        // マスターリストのうち、現在も習得済みのカードだけを実際のデッキに使う（未所持カードは除外）
        for (ResourceLocation id : deck.getMasterCardList()) {
            if (deck.getOwnedCards().contains(id)) all.add(id);
        }

        deck.getDrawPile().clear();
        deck.getHand().clear();
        deck.getDiscardPile().clear();
        deck.getExclusionPile().clear();

        Collections.shuffle(all);
        deck.getDrawPile().addAll(all);

        deck.getBuffState().resetAll();
        player.setAbsorptionAmount(0f); // 元気（衝撃吸収）を0にリセット

        // 初手確定カード（例：静かな意志）を先に手札へ強制的に加える
        List<ResourceLocation> guaranteed = new ArrayList<>();
        for (ResourceLocation id : new ArrayList<>(deck.getDrawPile())) {
            CardRegistry.get(id).ifPresent(def -> {
                if (def.isGuaranteedFirstDraw() && guaranteed.size() < HAND_SIZE) {
                    guaranteed.add(id);
                }
            });
        }
        for (ResourceLocation g : guaranteed) {
            deck.getDrawPile().remove(g);
            deck.getHand().add(g);
        }

        int remaining = HAND_SIZE - deck.getHand().size();
        drawCards(deck, remaining);

        deck.setSelectedIndex(0);
        deck.setInitialized(true);

        player.displayClientMessage(Component.literal("【デッキがリセットされました】").withStyle(ChatFormatting.AQUA), true);
    }

    /**
     * 山札からcount枚引いて手札へ加える。山札が足りない場合は捨て札をシャッフルして山札に補充する（仕様2末尾）。
     */
    private static void drawCards(IDeckData deck, int count) {
        for (int i = 0; i < count; i++) {
            if (deck.getDrawPile().isEmpty()) {
                if (deck.getDiscardPile().isEmpty()) {
                    // 山札にも捨て札にも何も残っていない（除外札のみ） -> これ以上引けない
                    break;
                }
                List<ResourceLocation> reshuffled = new ArrayList<>(deck.getDiscardPile());
                deck.getDiscardPile().clear();
                Collections.shuffle(reshuffled);
                deck.getDrawPile().addAll(reshuffled);
            }
            if (deck.getDrawPile().isEmpty()) break;
            deck.getHand().add(deck.getDrawPile().remove(0));
        }
    }

    /**
     * 1アクションを処理する（仕様2の手順1〜5）。
     * @param skip trueの場合はカードを発動せず、手札をすべて捨てて新しい手札を引く（スキップ）
     */
    public static void performAction(ServerPlayer player, IDeckData deck, boolean skip) {
        if (!deck.isInitialized() || deck.getHand().isEmpty()) {
            resetDeck(player, deck);
            return;
        }

        int idx = deck.getSelectedIndex();
        if (idx < 0 || idx >= deck.getHand().size()) idx = 0;

        if (!skip) {
            ResourceLocation cardId = deck.getHand().get(idx);
            CardDefinition def = CardRegistry.get(cardId).orElse(null);
            if (def == null) {
                skip = true;
            } else {
                // プロデューサーランク（進捗）チェック。手札上で減光表示されるため、テキストでは重ねて説明しない。
                if (def.getRequiredAdvancement() != null && !AdvancementHelper.hasAdvancement(player, def.getRequiredAdvancement())) {
                    return; // カード使用をキャンセルし、それ以外の状態は一切変化させない
                }
                // プロデューサーランク（Pレベル）チェック
                if (def.getRequiredPLevel() > 0 && deck.getPLevel() < def.getRequiredPLevel()) {
                    return; // カード使用をキャンセルし、それ以外の状態は一切変化させない
                }
                // 使用条件チェック（例：「集中が3以上の場合、使用可」）。
                // 満たさない場合は黙って不発にせず、選択そのものをキャンセルする（不発ではなく無選択扱い）。
                if (!def.getUsability().canUse(player, deck)) {
                    return; // カード使用をキャンセルし、それ以外の状態は一切変化させない
                }
                // コスト（体力）チェック・消費。消費体力減少中は軽減する（本Modでは一律 -2 の簡略化）
                if (def.getHpCost() > 0) {
                    int cost = def.getHpCost();
                    if (deck.getBuffState().getCostReductionTurns() > 0) {
                        cost = Math.max(0, cost - 2);
                    }
                    if (cost > 0) GenkiHelper.consumeCost(player, cost);
                }
                // 効果発動。「次カード2回発動」が残っていれば2回発動する。
                int times = 1;
                if (deck.getBuffState().getDoubleNextCards() > 0) {
                    times = 2;
                    deck.getBuffState().consumeDoubleNextCard();
                }
                for (int t = 0; t < times; t++) {
                    def.getEffect().apply(player, deck);
                }

                // 「演出計画」等の汎用パッシブ：カード使用のたびに元気+2 × スタック数
                // （同じ効果を持つカードを重複して使った場合、その分だけ効果が重複発動するようスタックで管理する）
                long encoreStacks = deck.getBuffState().getCustomCounter("encore_genki_stacks");
                if (encoreStacks > 0) {
                    GenkiHelper.addGenki(player, 2f * encoreStacks);
                }

                // 追加ドロー予約の解決（「スポットライト」「一発勝負」等）
                long pendingDraw = deck.getBuffState().getPendingDraw();
                if (pendingDraw > 0) {
                    drawCards(deck, (int) Math.min(pendingDraw, Integer.MAX_VALUE));
                    deck.getBuffState().clearPendingDraw();
                }

                // 使用済みカードの行き先: 通常カードは捨て札へ、レッスン中1回カードは除外へ
                deck.getHand().remove(idx);
                if (def.getType() == CardType.NORMAL) {
                    deck.getDiscardPile().add(cardId);
                } else {
                    deck.getExclusionPile().add(cardId);
                }

                // 「シュプレヒコール」等：スキルカード追加使用ボーナス。
                // ボーナス残があり、かつ残り手札がまだあるなら、ターンを終えずに続けて選択させる。
                if (deck.getBuffState().getBonusActions() > 0 && !deck.getHand().isEmpty()) {
                    deck.getBuffState().consumeBonusAction();
                    deck.setSelectedIndex(0);
                    return; // 捨て札送り・新規ドローを行わず、同じ手札から続けて選べる状態を維持する
                }
            }
        }

        // ボーナスはターンをまたいで持ち越さない
        deck.getBuffState().clearBonusActions();

        // 使わなかった残りの手札もすべて捨て札へ（★学マス重要仕様）
        deck.getDiscardPile().addAll(deck.getHand());
        deck.getHand().clear();

        // 「天真爛漫」等の汎用パッシブ：ターン終了時、集中が3以上ならさらに集中+2 × スタック数
        long focusPerTurnStacks = deck.getBuffState().getCustomCounter("focus_per_turn_stacks");
        if (focusPerTurnStacks > 0 && deck.getBuffState().getFocusStacks() >= 3) {
            deck.getBuffState().addFocus(2 * focusPerTurnStacks);
        }

        // 継続パラメータ（「至高のエンタメ」等）：ターン終了時にターゲットへその値ぶんダメージ
        long paramPerTurn = deck.getBuffState().getParamPerTurn();
        if (paramPerTurn > 0) {
            LivingEntity target = TargetingHelper.getLookTarget(player);
            if (target != null) {
                target.hurt(player.level().damageSources().magic(),
                        ScoreMath.calculateDamage(paramPerTurn, deck.getBuffState()));
            }
        }

        // 消費体力減少の残ターンを1減らす
        deck.getBuffState().tickCostReductionTurn();

        // 山札から新たに3枚ドロー
        drawCards(deck, HAND_SIZE);
        deck.setSelectedIndex(0);
    }

    public static void changeSelection(IDeckData deck, int delta) {
        if (deck.getHand().isEmpty()) return;
        deck.setSelectedIndex(deck.getSelectedIndex() + delta);
    }

    /**
     * 今このタイミングでカードを選択・発動できるかどうかを判定する（進捗・Pレベル・個別使用条件をすべて含む）。
     * {@link #performAction} 内の各チェックと同じ条件を、UI側の減光表示用に単一のbooleanとして提供する。
     */
    public static boolean isUsable(ServerPlayer player, IDeckData deck, CardDefinition def) {
        if (def.getRequiredAdvancement() != null && !AdvancementHelper.hasAdvancement(player, def.getRequiredAdvancement())) {
            return false;
        }
        if (def.getRequiredPLevel() > 0 && deck.getPLevel() < def.getRequiredPLevel()) {
            return false;
        }
        return def.getUsability().canUse(player, deck);
    }
}

