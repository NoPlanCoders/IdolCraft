package com.idolcraft.client;

import com.idolcraft.client.gui.CardWorkshopScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * サーバー/共通コードから安全に呼び出すためのクライアント専用の画面オープナー。
 * （共通コードから直接 Minecraft クラスを参照するとサーバー環境でクラッシュするため DistExecutor 経由で呼ぶ）
 */
@OnlyIn(Dist.CLIENT)
public final class ClientScreenOpener {

    private ClientScreenOpener() {}

    public static void openCardWorkshop() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new CardWorkshopScreen());
        }
    }
}

