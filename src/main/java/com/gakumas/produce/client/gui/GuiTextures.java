package com.gakumas.produce.client.gui;

import com.gakumas.produce.GakumasProduceMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * デッキ編成画面・HUDなどで共用するGUIテクスチャの参照とその実ピクセルサイズ。
 * gen_gui_textures.py（SCALE=3）で生成したものと対応させている。
 */
public final class GuiTextures {

    private GuiTextures() {}

    public static final ResourceLocation PANEL = tex("deck_editor_panel.png");
    public static final ResourceLocation HEADER = tex("deck_editor_header.png");
    public static final ResourceLocation SLOT = tex("card_slot.png");
    public static final ResourceLocation SLOT_DECK = tex("card_slot_deck.png");
    public static final ResourceLocation SLOT_HOVER = tex("card_slot_hover.png");
    public static final ResourceLocation PILL = tex("button_pill.png");
    public static final ResourceLocation PILL_CONFIRM = tex("button_confirm.png");
    public static final ResourceLocation PILL_RESET = tex("button_reset.png");
    public static final ResourceLocation PILL_CANCEL = tex("button_cancel.png");
    public static final ResourceLocation ICON_FOCUS = tex("icon_focus.png");
    public static final ResourceLocation ICON_GOOD_CONDITION = tex("icon_good_condition.png");
    public static final ResourceLocation ICON_GREAT_CONDITION = tex("icon_great_condition.png");
    public static final ResourceLocation TOOLTIP_PANEL = tex("tooltip_panel.png");

    // 実ファイルのピクセルサイズ
    public static final int PANEL_TEX_W = 2160;
    public static final int PANEL_TEX_H = 1620;
    public static final int HEADER_TEX_W = 1380;
    public static final int HEADER_TEX_H = 372;
    public static final int SLOT_TEX = 312;
    public static final int PILL_TEX_W = 702;
    public static final int PILL_TEX_H = 234;
    public static final int BUFF_ICON_TEX = 43;
    public static final int TOOLTIP_PANEL_TEX_W = 720;
    public static final int TOOLTIP_PANEL_TEX_H = 522;
    /** テクスチャ全体の高さのうち、吹き出し本体（角丸矩形部分、しっぽを除く）が占める割合 */
    public static final float TOOLTIP_BODY_RATIO = 480f / 522f;

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(GakumasProduceMod.MOD_ID, "textures/gui/" + name);
    }

    /**
     * 角丸パネル等の「滑らかな縁」を持つテクスチャをバインドし、線形（バイリニア）フィルタを適用する。
     * Minecraftは既定でニアレストネイバー（最近傍）フィルタを使うため、大きく縮小表示される
     * カスタムUIパネルの角丸・影がギザギザに見えてしまう問題への対策。
     * ※ドット絵として作っているバフアイコン（ICON_FOCUS等）には使わないこと（ぼやけてしまうため）。
     */
    public static void bindSmooth(ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }
}
