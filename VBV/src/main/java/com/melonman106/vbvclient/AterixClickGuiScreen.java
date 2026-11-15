package com.melonman106.vbvclient;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Purple/black module click GUI for Aterix Client. */
public final class AterixClickGuiScreen extends Screen {
    private static final String[] CATEGORIES = {"All", "Combat", "Movement", "Render", "HUD", "Utility"};
    private static final String[][] MODULES = {
        {"killaura", "KillAura", "Combat"},
        {"fullbright", "Fullbright", "Utility"},
        {"zoom", "Zoom", "Utility"},
        {"simple-hud", "HUD", "HUD"},
        {"armor-hud", "Armor HUD", "HUD"},
        {"appleskin", "AppleSkin", "HUD"},
        {"crosshair", "Custom Crosshair", "HUD"},
        {"betterf3", "BetterF3", "Render"},
        {"perf-meter", "Performance Meter", "Render"},
        {"player-radar", "Player Radar", "Render"},
        {"player-esp", "Player ESP", "Render"},
        {"auto-sprint", "Auto Sprint", "Utility"},
        {"speed", "Speed", "Movement"},
        {"no-fall", "NoFall", "Movement"},
        {"elytra-fly", "ElytraFly", "Movement"},
        {"minimap", "Minimap", "Render"},
        {"jade", "Target Info", "HUD"},
        {"shulker-viewer", "Shulker Preview", "Utility"},
        {"first-person", "First Person Arms", "Render"},
        {"lod", "Distant Terrain LOD", "Render"},
        {"cem", "Custom Entity Models", "Render"}
    };

    private final Map<String, Button> moduleButtons = new LinkedHashMap<>();
    private final Map<String, Button> categoryButtons = new LinkedHashMap<>();
    private String category = "All";
    private long openedAt;

    private int panelWidth() { return Math.min(390, Math.max(240, width - 16)); }
    private int moduleRows() { return (MODULES.length + 1) / 2; }
    private int rowPitch() { return Math.max(22, Math.min(31, (height - 105) / Math.max(1, moduleRows()))); }
    private int panelHeight() { return 54 + moduleRows() * rowPitch() + 35; }
    private int panelTop() { return Math.max(8, (height - panelHeight()) / 2); }

    public AterixClickGuiScreen() {
        super(Component.literal("Aterix Client"));
    }

    @Override
    protected void init() {
        super.init();
        openedAt = System.currentTimeMillis();
        VBVClient.init();
        int panelWidth = panelWidth();
        int left = (width - panelWidth) / 2;
        int top = panelTop();
        int pitch = rowPitch();
        int buttonHeight = Math.min(25, pitch - 3);
        int tabGap = 4;
        int tabWidth = (panelWidth - 24 - tabGap * (CATEGORIES.length - 1)) / CATEGORIES.length;

        categoryButtons.clear();
        for (int i = 0; i < CATEGORIES.length; i++) {
            final String name = CATEGORIES[i];
            Button tab = Button.builder(Component.literal(name), b -> {
                category = name;
                refreshButtons();
            }).bounds(left + 12 + i * (tabWidth + tabGap), top + 29, tabWidth, 18).build();
            categoryButtons.put(name, tab);
            addRenderableWidget(tab);
        }

        moduleButtons.clear();
        int gap = 8;
        int buttonWidth = (panelWidth - 32 - gap) / 2;
        int row = 0;
        for (String[] module : MODULES) {
            final String id = module[0];
            Button button = Button.builder(Component.literal(moduleLabel(id, module[1])), b -> {
                toggle(id);
                b.setMessage(Component.literal(moduleLabel(id, module[1])));
            }).bounds(left + 12 + (row % 2) * (buttonWidth + gap),
                      top + 54 + (row / 2) * pitch, buttonWidth, buttonHeight).build();
            moduleButtons.put(id, button);
            addRenderableWidget(button);
            row++;
        }

        addRenderableWidget(Button.builder(Component.literal("Done  (Right Shift)"), b -> onClose())
            .bounds(width / 2 - 78, top + 54 + moduleRows() * pitch + 5, 156, 20).build());
        refreshButtons();
    }

    private void refreshButtons() {
        for (Map.Entry<String, Button> entry : categoryButtons.entrySet()) {
            entry.getValue().setMessage(Component.literal(
                entry.getKey().equals(category) ? "» " + entry.getKey() : entry.getKey()));
        }
        for (int i = 0; i < MODULES.length; i++) {
            String[] module = MODULES[i];
            Button button = moduleButtons.get(module[0]);
            if (button != null) {
                button.visible = category.equals("All") || category.equals(module[2]);
                button.setMessage(Component.literal(moduleLabel(module[0], module[1])));
            }
        }
    }

    private static String moduleLabel(String id, String name) {
        return (isEnabled(id) ? "§a[ON] " : "§7[OFF] ") + name;
    }

    private static boolean isEnabled(String id) {
        switch (id) {
            case "killaura": return VBVKillAura.enabled;
            case "fullbright": return VBVExtraFeatures.isGammaFeatureEnabled();
            case "zoom": return VBVExtraFeatures.isZoomEnabled();
            case "simple-hud": return VBVSimpleHudConfig.enabled;
            case "armor-hud": return VBVArmorHudConfig.enabled;
            case "appleskin": return VBVAppleSkin.isEnabled();
            case "crosshair": return VBVCrosshairConfig.enabled;
            case "betterf3": return VBVExtraFeatures.isBetterF3Enabled();
            case "perf-meter": return VBVPerf.isMeter();
            case "player-radar": return VBVPlayerRadar.enabled;
            case "player-esp": return VBVPlayerEsp.enabled;
            case "auto-sprint": return VBVMovementFeatures.autoSprint;
            case "speed": return VBVMovementFeatures.speed;
            case "no-fall": return VBVMovementFeatures.noFall;
            case "elytra-fly": return VBVMovementFeatures.elytraFly;
            case "minimap": return VBVMiniMap.enabled;
            case "jade": return VBVJadeHud.enabled;
            case "shulker-viewer": return VBVShulkerViewer.enabled;
            case "first-person": return VBVFirstPersonAnimations.isEnabled();
            case "lod": return VBVLod.getLevel() != VBVLod.OFF;
            case "cem": return VBVCem.isEnabled();
            default: return false;
        }
    }

    private static void toggle(String id) {
        switch (id) {
            case "killaura": VBVKillAura.enabled = !VBVKillAura.enabled; break;
            case "fullbright": VBVExtraFeatures.toggleGammaFeature(); break;
            case "zoom": VBVExtraFeatures.toggleZoomEnabled(); break;
            case "simple-hud": VBVSimpleHudConfig.enabled = !VBVSimpleHudConfig.enabled; break;
            case "armor-hud": VBVArmorHudConfig.enabled = !VBVArmorHudConfig.enabled; break;
            case "appleskin": VBVAppleSkin.toggleEnabled(); break;
            case "crosshair": VBVCrosshairConfig.enabled = !VBVCrosshairConfig.enabled; break;
            case "betterf3": VBVExtraFeatures.toggleBetterF3Enabled(); break;
            case "perf-meter": VBVPerf.toggleMeter(); break;
            case "player-radar": VBVPlayerRadar.enabled = !VBVPlayerRadar.enabled; break;
            case "player-esp": VBVPlayerEsp.setEnabled(!VBVPlayerEsp.enabled); break;
            case "auto-sprint": VBVMovementFeatures.autoSprint = !VBVMovementFeatures.autoSprint; break;
            case "speed": VBVMovementFeatures.speed = !VBVMovementFeatures.speed; break;
            case "no-fall": VBVMovementFeatures.noFall = !VBVMovementFeatures.noFall; break;
            case "elytra-fly": VBVMovementFeatures.elytraFly = !VBVMovementFeatures.elytraFly; break;
            case "minimap": VBVMiniMap.enabled = !VBVMiniMap.enabled; break;
            case "jade": VBVJadeHud.enabled = !VBVJadeHud.enabled; break;
            case "shulker-viewer": VBVShulkerViewer.toggle(); break;
            case "first-person": VBVFirstPersonAnimations.toggle(); break;
            case "lod": VBVLod.setLevel(VBVLod.getLevel() == VBVLod.OFF ? VBVLod.LOW : VBVLod.OFF); break;
            case "cem": VBVCem.toggle(); break;
            default: return;
        }
        VBVConfigStore.saveAll();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = panelWidth();
        int left = (width - panelWidth) / 2;
        int top = panelTop();
        int bottom = top + panelHeight();
        // Paint the panel BEFORE native widgets. Painting it after super.extractRenderState
        // covered every module button, making the menu look empty and unclickable.
        graphics.fill(left - 2, top - 2, left + panelWidth + 2, bottom + 2, 0xFF08060D);
        graphics.fill(left, top, left + panelWidth, bottom, 0xF20E0B16);
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFF512477);
        float progress = Math.min(1.0F, Math.max(0.0F, (System.currentTimeMillis() - openedAt) / 320.0F));
        float eased = progress * progress * (3.0F - 2.0F * progress);
        int accentWidth = Math.max(1, Math.round(panelWidth * eased));
        graphics.fill(left, top, left + accentWidth, top + 3, 0xFFB56BFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        // Fade the whole menu in without permanently drawing the panel over its controls.
        int fadeAlpha = Math.round(115.0F * (1.0F - eased));
        if (fadeAlpha > 0) graphics.fill(left, top, left + panelWidth, bottom, (fadeAlpha << 24));
        String title = "ATERIX CLIENT";
        graphics.text(font, title, width / 2 - font.width(title) / 2, top + 9, 0xFFE8D8FF, true);
        String hint = "Right Shift  •  Toggle modules  •  Settings save automatically";
        graphics.text(font, hint, width / 2 - font.width(hint) / 2, bottom - 15, 0xFF9C91AA, false);
    }

    @Override
    public void onClose() {
        VBVConfigStore.saveAll();
        VBVNav.open(null);
    }
}
