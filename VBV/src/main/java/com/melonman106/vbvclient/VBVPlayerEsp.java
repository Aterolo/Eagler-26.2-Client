package com.melonman106.vbvclient;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side player ESP. Uses the vanilla glowing render flag on entities
 * already known to the local client; it does not query or reveal server-hidden entities.
 */
public final class VBVPlayerEsp {
    public static boolean enabled;
    private static final Map<Entity, Boolean> PREVIOUS = new IdentityHashMap<>();
    private static Object lastLevel;

    private VBVPlayerEsp() {}

    public static void tick(Minecraft client) {
        if (client == null) return;
        if (client.level != lastLevel) {
            restore();
            lastLevel = client.level;
        }
        if (!enabled || client.level == null || client.player == null) {
            restore();
            return;
        }

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof Player) || entity == client.player || !entity.isAlive()) continue;
            if (!PREVIOUS.containsKey(entity)) {
                boolean wasGlowing = entity.hasGlowingTag();
                PREVIOUS.put(entity, wasGlowing);
                if (!wasGlowing) entity.setGlowingTag(true);
            }
        }

        // Restore entities that have left the loaded client world.
        PREVIOUS.entrySet().removeIf(entry -> {
            Entity entity = entry.getKey();
            if (entity.level() == client.level && entity.isAlive()) return false;
            if (!entry.getValue()) entity.setGlowingTag(false);
            return true;
        });
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) restore();
    }

    private static void restore() {
        for (Map.Entry<Entity, Boolean> entry : PREVIOUS.entrySet()) {
            if (!entry.getValue()) entry.getKey().setGlowingTag(false);
        }
        PREVIOUS.clear();
    }
}
