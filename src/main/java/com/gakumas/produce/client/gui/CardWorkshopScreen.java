package com.gakumas.produce.client.gui;

import com.gakumas.produce.card.CardCatalog;
import com.gakumas.produce.card.CardDefinition;
import com.gakumas.produce.card.CardRarity;
import com.gakumas.produce.card.CardRegistry;
import com.gakumas.produce.client.ClientDeckState;
import com.gakumas.produce.item.CardMaterialItem;
import com.gakumas.produce.network.NetworkHandler;
import com.gakumas.produce.network.packet.CraftCardPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * スキルカード作業台のGUI ─ 学マス風の白ベース＋すりガラス。
 * 所持している素材と同じレア度で、かつPレベルで解放済みの任意のカードを選んで習得できる。
 */
public class CardWorkshopScreen extends Screen {

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 320;
    private static final int HEADER_W = 300;
    private static final int HEADER_H = 74;
    private static final int ROW_H = 24;
    private static final int VISIBLE_ROWS = 8;

    private static final int COLOR_TITLE   = 0xFF3A3550;
    private static final int COLOR_LABEL   = 0xFF6B6880;
    private static final int COLOR_SUB     = 0xFF8A87A0;
    private static final int COLOR_BACKDROP = 0x88283050;
    private static final int COLOR_ROW      = 0xFFF6F8FD;
    private static final int COLOR_ROW_HOV  = 0xFFEAF6FF;
    private static final int COLOR_ROW_LOCK = 0xFFECEEF5;
    private static final int COLOR_OWNED    = 0xFF33A65B;
    private static final int COLOR_LOCKED   = 0xFF9A97AC;
    private static final int COLOR_CRAFT    = 0xFF3AB2EC;

    private final List<CardDefinition> cards = new ArrayList<>();
    private int panelX, panelY, listX, listW, listY;
    private int scroll = 0;

    public CardWorkshopScreen() {
        super(Component.literal("スキルカード作業台"));
    }

    @Override
    protected void init() {
        super.init();
        cards.clear();
        cards.addAll(CardRegistry.all());
        cards.sort(Comparator
                .comparingInt((CardDefinition d) -> CardCatalog.rarityOf(d.getId()).ordinal())
                .thenComparingInt(CardDefinition::getRequiredPLevel)
                .thenComparing(CardDefinition::getDisplayName));
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;
        listX = panelX + 20;
        listW = PANEL_W - 40;
        listY = panelY + 74;
        scroll = 0;
    }

    /** インベントリ内のレア度別素材所持数を数える */
    private Map<CardRarity, Integer> materialCounts() {
        Map<CardRarity, Integer> counts = new EnumMap<>(CardRarity.class);
        for (CardRarity r : CardRarity.values()) counts.put(r, 0);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return counts;
        for (ItemStack stack : mc.player.getInventory().items) {
            if (stack.getItem() instanceof CardMaterialItem mat) {
                counts.merge(mat.getRarity(), stack.getCount(), Integer::sum);
            }
        }
        return counts;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, COLOR_BACKDROP);
        blit(g, GuiTextures.PANEL, panelX, panelY, PANEL_W, PANEL_H, GuiTextures.PANEL_TEX_W, GuiTextures.PANEL_TEX_H);

        int hx = panelX + (PANEL_W - HEADER_W) / 2;
        int hy = panelY - 18;
        blit(g, GuiTextures.HEADER, hx, hy, HEADER_W, HEADER_H, GuiTextures.HEADER_TEX_W, GuiTextures.HEADER_TEX_H);
        g.drawCenteredString(this.font, "スキルカード作業台", panelX + PANEL_W / 2, hy + HEADER_H / 2 - 5, COLOR_TITLE);

        // 所持素材の表示
        Map<CardRarity, Integer> counts = materialCounts();
        StringBuilder sb = new StringBuilder("所持素材  ");
        for (CardRarity r : CardRarity.values()) sb.append(r.getLabel()).append(":").append(counts.get(r)).append("  ");
        g.drawString(this.font, sb.toString().trim(), listX, panelY + 58, COLOR_LABEL, false);

        int pLevel = ClientDeckState.getPLevel();
        int maxScroll = Math.max(0, cards.size() - VISIBLE_ROWS);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int idx = scroll + row;
            if (idx >= cards.size()) break;
            CardDefinition def = cards.get(idx);
            int y = listY + row * ROW_H;
            renderRow(g, def, y, counts, pLevel, mouseX, mouseY);
        }

        if (maxScroll > 0) {
            g.drawCenteredString(this.font, "▲▼ スクロール", panelX + PANEL_W / 2, listY + VISIBLE_ROWS * ROW_H + 2, COLOR_SUB);
        }
    }

    private void renderRow(GuiGraphics g, CardDefinition def, int y, Map<CardRarity, Integer> counts, int pLevel, int mx, int my) {
        CardRarity rarity = CardCatalog.rarityOf(def.getId());
        boolean owned = ClientDeckState.getOwnedCards().contains(def.getId());
        boolean pOk = def.getRequiredPLevel() <= pLevel;
        boolean hasMat = counts.getOrDefault(rarity, 0) > 0;
        boolean craftable = !owned && pOk && hasMat;
        boolean hovered = mx >= listX && mx < listX + listW && my >= y && my < y + ROW_H - 3;

        int bg = owned || !pOk ? COLOR_ROW_LOCK : (craftable && hovered ? COLOR_ROW_HOV : COLOR_ROW);
        g.fill(listX, y, listX + listW, y + ROW_H - 3, bg);
        g.fill(listX, y, listX + 3, y + ROW_H - 3, rarity.getColor());

        Item item = ForgeRegistries.ITEMS.getValue(def.getId());
        if (item != null) g.renderItem(new ItemStack(item), listX + 7, y + 1);

        int nameColor = (owned || !pOk) ? COLOR_LOCKED : COLOR_TITLE;
        g.drawString(this.font, def.getDisplayName(), listX + 29, y + 5, nameColor, false);

        String right;
        int rColor;
        if (owned) { right = "習得済み"; rColor = COLOR_OWNED; }
        else if (!pOk) { right = "PLv." + def.getRequiredPLevel(); rColor = COLOR_LOCKED; }
        else if (!hasMat) { right = rarity.getLabel() + "素材なし"; rColor = COLOR_LOCKED; }
        else { right = "解放 ▶ " + rarity.getLabel() + "×1"; rColor = COLOR_CRAFT; }
        int rw = this.font.width(right);
        g.drawString(this.font, right, listX + listW - rw - 6, y + 5, rColor, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int pLevel = ClientDeckState.getPLevel();
            Map<CardRarity, Integer> counts = materialCounts();
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int idx = scroll + row;
                if (idx >= cards.size()) break;
                int y = listY + row * ROW_H;
                if (mx >= listX && mx < listX + listW && my >= y && my < y + ROW_H - 3) {
                    CardDefinition def = cards.get(idx);
                    CardRarity rarity = CardCatalog.rarityOf(def.getId());
                    boolean owned = ClientDeckState.getOwnedCards().contains(def.getId());
                    boolean pOk = def.getRequiredPLevel() <= pLevel;
                    boolean hasMat = counts.getOrDefault(rarity, 0) > 0;
                    if (!owned && pOk && hasMat) {
                        NetworkHandler.CHANNEL.sendToServer(new CraftCardPacket(def.getId()));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        scroll -= (int) Math.signum(delta);
        int maxScroll = Math.max(0, cards.size() - VISIBLE_ROWS);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        return true;
    }

    private void blit(GuiGraphics g, ResourceLocation tex, int x, int y, int dw, int dh, int tw, int th) {
        GuiTextures.bindSmooth(tex);
        g.blit(tex, x, y, dw, dh, 0f, 0f, tw, th, tw, th);
    }

    @Override public boolean isPauseScreen() { return false; }
}
