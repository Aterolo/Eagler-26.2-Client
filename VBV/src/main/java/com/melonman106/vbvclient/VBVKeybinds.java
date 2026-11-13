package com.melonman106.vbvclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;

/** Mods-screen and world-map navigation hotkeys on the native keyboard event bridge. */
public final class VBVKeybinds {
    private static final int KEY_V = 86;
    private static final int KEY_M = 77;
    private static final int KEY_RIGHT_SHIFT = 344;

    private VBVKeybinds() {}

    /** Returns true when a VBV hotkey consumed the event. */
    public static boolean handle(KeyEvent event) {
        if (event == null) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.gui.screen();

        // Right Shift toggles the Veydris ClickGUI and is intentionally checked
        // before modifier guards so the key can close the GUI while it is open.
        if (event.key() == KEY_RIGHT_SHIFT && !event.hasControlDown() && !event.hasAltDown()) {
            if (screen instanceof VeydrisClickGuiScreen) {
                VBVNav.open(null);
                return true;
            }
            if (screen == null && minecraft.player != null) {
                VBVClient.init();
                VBVNav.open(new VeydrisClickGuiScreen());
                return true;
            }
        }

        if (event.hasControlDown() || event.hasAltDown() || event.hasShiftDown()) {
            return false;
        }

        if (screen != null || minecraft.player == null) {
            return false;
        }

        if (event.key() == KEY_V) {
            VBVClient.init();
            HerzVBVBridge.initialize();
            VBVNav.open(new VBVModsScreen(null));
            return true;
        }

        if (event.key() == KEY_M) {
            VBVClient.init();
            HerzVBVBridge.initialize();
            VBVNav.open(new VBVWorldMapScreen(null));
            return true;
        }

        return false;
    }
}
