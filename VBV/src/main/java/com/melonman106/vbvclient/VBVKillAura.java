package com.melonman106.vbvclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;

/**
 * Optional client-side nearest-player attack helper.
 * It only targets players already present in the local client world.
 */
public final class VBVKillAura {
    public static boolean enabled;
    private static int tickDelay;

    private VBVKillAura() {}

    public static void tick(Minecraft client) {
        if (client == null || client.player == null || client.level == null
                || client.gameMode == null || !enabled) {
            tickDelay = 0;
            return;
        }
        if (++tickDelay < 10) return;
        tickDelay = 0;

        Player self = client.player;
        Player nearest = null;
        double nearestDistance = 4.2D * 4.2D;
        for (var entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof Player target) || target == self || !target.isAlive()) continue;
            double distance = target.distanceToSqr(self);
            if (distance < nearestDistance && self.hasLineOfSight(target)) {
                nearest = target;
                nearestDistance = distance;
            }
        }
        if (nearest != null) {
            client.gameMode.attack(self, nearest);
            self.swing(InteractionHand.MAIN_HAND);
        }
    }
}
