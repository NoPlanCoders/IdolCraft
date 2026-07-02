package com.gakumas.produce.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;

public final class KeyBindings {

    public static final String CATEGORY = "key.categories.gakumas_produce";

    public static final KeyMapping RESET_DECK = new KeyMapping(
            "key.gakumas_produce.reset_deck",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            CATEGORY
    );

    public static final KeyMapping SKIP_CARD = new KeyMapping(
            "key.gakumas_produce.skip_card",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            CATEGORY
    );

    public static final KeyMapping OPEN_DECK_EDITOR = new KeyMapping(
            "key.gakumas_produce.open_deck_editor",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_D,
            CATEGORY
    );

    private KeyBindings() {}
}
