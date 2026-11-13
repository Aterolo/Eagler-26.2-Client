package com.melonman106.vbvclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side movement helpers. Server movement validation still applies;
 * these features do not alter packets or bypass server-side checks.
 */
public final class VBVMovementFeatures {
    public static boolean autoSprint;
    public static boolean speed;
    public static boolean noFall;
    public static boolean elytraFly;

    private VBVMovementFeatures() {}

    public static void tick(Minecraft client) {
        if (client == null || client.player == null) return;
        var player = client.player;

        if (autoSprint) {
            boolean canSprint = !player.isCrouching()
                && player.getFoodData().getFoodLevel() > 6
                && player.input.hasForwardImpulse();
            player.setSprinting(canSprint);
        }

        // A small forward acceleration, applied only while grounded and moving.
        // This is deliberately conservative because servers can correct movement.
        if (speed && player.onGround() && !player.isCrouching()
                && player.input.hasForwardImpulse()) {
            float yaw = player.getYRot() * ((float) Math.PI / 180.0F);
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(velocity.x - Math.sin(yaw) * 0.018D,
                velocity.y, velocity.z + Math.cos(yaw) * 0.018D);
        }

        // Local fall-distance suppression. Servers may still apply fall damage
        // from their own movement state, so this is not a packet bypass.
        if (noFall && player.getDeltaMovement().y < 0.0D) {
            player.fallDistance = 0.0F;
        }

        // Elytra steering/boost is client-side and subject to server validation.
        if (elytraFly && player.isFallFlying()) {
            Vec3 look = player.getLookAngle();
            Vec3 velocity = player.getDeltaMovement();
            double boost = player.input.hasForwardImpulse() ? 0.035D : 0.0D;
            player.setDeltaMovement(
                velocity.x + look.x * boost,
                velocity.y + look.y * boost,
                velocity.z + look.z * boost
            );
        }
    }
}
