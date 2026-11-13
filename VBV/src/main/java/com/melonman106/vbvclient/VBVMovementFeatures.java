package com.melonman106.vbvclient;

import net.minecraft.client.Minecraft;

/** Small client-side movement helper; server movement rules still apply. */
public final class VBVMovementFeatures {
    public static boolean autoSprint;

    private VBVMovementFeatures() {}

    public static void tick(Minecraft client) {
        if (client == null || client.player == null) return;
        if (!autoSprint) return;

        var player = client.player;
        boolean canSprint = !player.isCrouching()
            && player.getFoodData().getFoodLevel() > 6
            && player.input.hasForwardImpulse();
        player.setSprinting(canSprint);
    }
}
