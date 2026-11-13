package com.melonman106.vbvclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

/**
 * Lightweight player radar overlay. It lists other player entities already
 * present in the client world; it does not request hidden server information.
 */
public final class VBVPlayerRadar {
    public static boolean enabled;

    private VBVPlayerRadar() {}

    public static void render(GuiGraphicsExtractor graphics) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        java.util.List<String> players = new java.util.ArrayList<>();
        for (var entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player target && target != mc.player && target.isAlive()) {
                int distance = Math.round(target.distanceTo(mc.player));
                players.add(target.getName().getString() + "  " + distance + "m");
                if (players.size() >= 8) break;
            }
        }

        int x = 6;
        int y = 54;
        int width = 150;
        int lineHeight = mc.font.lineHeight + 3;
        int height = lineHeight * (players.size() + 1) + 8;
        graphics.fill(x - 3, y - 3, x + width, y + height, 0xA9080610);
        graphics.fill(x - 3, y - 3, x + width, y - 1, 0xFF9B55F5);
        graphics.text(mc.font, "VEYDRIS PLAYER RADAR", x + 3, y + 2, 0xFFE8D8FF, true);

        if (players.isEmpty()) {
            graphics.text(mc.font, "No other players nearby", x + 3, y + lineHeight + 3, 0xFFAAA0B8, false);
        } else {
            for (int i = 0; i < players.size(); i++) {
                graphics.text(mc.font, players.get(i), x + 3, y + lineHeight + 3 + i * lineHeight,
                    0xFFFFFFFF, true);
            }
        }
    }
}
