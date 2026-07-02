package com.gakumas.produce.client.gui;

import com.gakumas.produce.client.ClientDeckState;
import com.gakumas.produce.item.HandbookItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Locale;

/**
 * 手帳所持中のみ表示されるHUD。
 * 左端：バフアイコン一覧（好調・絶好調は残り秒数をリアルタイム表示）。
 * 下部：手札3枚。選択中のカードは少し上にハイライト。
 */
public class GakumasHudOverlay implements IGuiOverlay {

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        boolean holdingHandbook = mc.player.getMainHandItem().getItem() instanceof HandbookItem
                || mc.player.getOffhandItem().getItem() instanceof HandbookItem;
        if (!holdingHandbook) return;
        if (mc.options.hideGui) return;

        renderBuffColumn(graphics, screenHeight);
        renderHand(mc, graphics, screenWidth, screenHeight);
    }

    private void renderBuffColumn(GuiGraphics graphics, int screenHeight) {
        int x = 6;
        int y = screenHeight / 2 - 40;
        int lineHeight = 14;

        int focus = ClientDeckState.getFocusStacks();
        int goodTicks = ClientDeckState.getGoodTicks();
        int greatTicks = ClientDeckState.getGreatTicks();

        if (focus > 0) {
            drawBuffLine(graphics, x, y, 0x55CCFF, "集中", "x" + focus);
            y += lineHeight;
        }
        if (goodTicks > 0) {
            drawBuffLine(graphics, x, y, 0xFFD24D, "好調", formatSeconds(goodTicks));
            y += lineHeight;
        }
        if (greatTicks > 0) {
            drawBuffLine(graphics, x, y, 0xFF5599, "絶好調", formatSeconds(greatTicks));
            y += lineHeight;
        }
    }

    private void drawBuffLine(GuiGraphics graphics, int x, int y, int color, String label, String value) {
        graphics.fill(x, y, x + 70, y + 12, 0x99000000);
        graphics.drawString(Minecraft.getInstance().font, Component.literal(label), x + 2, y + 2, color, false);
        graphics.drawString(Minecraft.getInstance().font, Component.literal(value), x + 40, y + 2, 0xFFFFFF, false);
    }

    private String formatSeconds(int ticks) {
        double seconds = ticks / 20.0;
        return String.format(Locale.US, "%.1fs", seconds);
    }

    private void renderHand(Minecraft mc, GuiGraphics graphics, int screenWidth, int screenHeight) {
        List<ResourceLocation> hand = ClientDeckState.getHand();
        if (hand.isEmpty()) return;

        int slotSize = 24;
        int spacing = 4;
        int totalWidth = hand.size() * slotSize + (hand.size() - 1) * spacing;
        int startX = screenWidth / 2 - totalWidth / 2;
        int baseY = screenHeight - 70;

        int selected = ClientDeckState.getSelectedIndex();

        for (int i = 0; i < hand.size(); i++) {
            int x = startX + i * (slotSize + spacing);
            int y = baseY - (i == selected ? 8 : 0); // 選択中のカードは少し上にハイライト

            int bg = (i == selected) ? 0xAAFFD24D : 0x88222222;
            graphics.fill(x - 2, y - 2, x + slotSize + 2, y + slotSize + 2, bg);

            Item item = ForgeRegistries.ITEMS.getValue(hand.get(i));
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                graphics.renderItem(stack, x, y);
                graphics.renderItemDecorations(mc.font, stack, x, y);
            }
        }
    }
}
