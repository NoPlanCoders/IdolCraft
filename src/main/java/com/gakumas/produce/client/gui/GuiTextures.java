package com.gakumas.produce.client.gui;

import com.gakumas.produce.GakumasProduceMod;
import net.minecraft.resources.ResourceLocation;

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
    public static final ResourceLocation ICON_FOCUS = tex("icon_focus.png");
    public static final ResourceLocation ICON_GOOD_CONDITION = tex("icon_good_condition.png");
    public static final ResourceLocation ICON_GREAT_CONDITION = tex("icon_great_condition.png");

    // 実ファイルのピクセルサイズ
    public static final int PANEL_TEX_W = 2160;
    public static final int PANEL_TEX_H = 1620;
    public static final int HEADER_TEX_W = 1380;
    public static final int HEADER_TEX_H = 372;
    public static final int SLOT_TEX = 312;
    public static final int PILL_TEX_W = 576;
    public static final int PILL_TEX_H = 264;
    public static final int BUFF_ICON_TEX = 96;

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(GakumasProduceMod.MOD_ID, "textures/gui/" + name);
    }
}
