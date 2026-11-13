package com.melonman106.vbvclient;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Purple/black module click GUI for Veydris Client. */
public final class VeydrisClickGuiScreen extends Screen {
    private static final String[] CATEGORIES = {"All", "Render", "HUD", "Utility"};
    private static final String[][] MODULES = {
        {"fullbright", "Fullbright", "Utility"},
        {"zoom", "Zoom", "Utility"},
        {"simple-hud", "HUD", "HUD"},
        {"armor-hud", "Armor HUD", "HUD"},
        {"appleskin", "AppleSkin", "HUD"},
        {"crosshair", "Custom Crosshair", "HUD"},
        {"entity-culling", "Entity Culling", "Render"},
        {"betterf3", "BetterF3", "Render"},
        {"perf-meter", "Performance Meter", "Render"}
    };

    private final Map<String, Button> moduleButtons = new LinkedHashMap<>();
    private final Map<String, Button> categoryButtons = new LinkedHashMap<>();
    private String category = "All";

    public VeydrisClickGuiScreen() {
        super(Component.literal("Veydris Client"));
    }

    @Override
    protected void init() {
        super.init();
        VBVClient.init();
        int panelWidth = Math.min(390, width - 24);
        int left = (width - panelWidth) / 2;
        int top = Math.max(24, height / 2 - 125);
        int tabGap = 4;
        int tabWidth = (panelWidth - 24 - tabGap * 3) / 4;

        categoryButtons.clear();
        for (int i = 0; i < CATEGORIES.length; i++) {
            final String name = CATEGORIES[i];
            Button tab = Button.builder(Component.literal(name), b -> {
                category = name;
                refreshButtons();
            }).bounds(left + 12 + i * (tabWidth + tabGap), top + 35, tabWidth, 20).build();
            categoryButtons.put(name, tab);
            addRenderableWidget(tab);
        }

        moduleButtons.clear();
        int gap = 8;
        int buttonWidth = (panelWidth - 32 - gap) / 2;
        int buttonHeight = 25;
        int row = 0;
        for (String[] module : MODULES) {
            final String id = module[0];
            Button button = Button.builder(Component.literal(moduleLabel(id, module[1])), b -> {
                toggle(id);
                b.setMessage(Component.literal(moduleLabel(id, module[1])));
            }).bounds(left + 12 + (row % 2) * (buttonWidth + gap),
                      top + 65 + (row / 2) * (buttonHeight + 6), buttonWidth, buttonHeight).build();
            moduleButtons.put(id, button);
            addRenderableWidget(button);
            row++;
        }

        addRenderableWidget(Button.builder(Component.literal("Done  (Right Shift)"), b -> onClose())
            .bounds(width / 2 - 78, top + 65 + ((MODULES.length + 1) / 2) * 31 + 8, 156, 20).build());
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
            case "fullbright": return VBVExtraFeatures.isGammaFeatureEnabled();
            case "zoom": return VBVExtraFeatures.isZoomEnabled();
            case "simple-hud": return VBVSimpleHudConfig.enabled;
            case "armor-hud": return VBVArmorHudConfig.enabled;
            case "appleskin": return VBVAppleSkin.isEnabled();
            case "crosshair": return VBVCrosshairConfig.enabled;
            case "entity-culling": return VBVEntityCulling.isEnabled();
            case "betterf3": return VBVExtraFeatures.isBetterF3Enabled();
            case "perf-meter": return VBVPerf.isMeter();
            default: return false;
        }
    }

    private static void toggle(String id) {
        switch (id) {
            case "fullbright": VBVExtraFeatures.toggleGammaFeature(); break;
            case "zoom": VBVExtraFeatures.toggleZoomEnabled(); break;
            case "simple-hud": VBVSimpleHudConfig.enabled = !VBVSimpleHudConfig.enabled; break;
            case "armor-hud": VBVArmorHudConfig.enabled = !VBVArmorHudConfig.enabled; break;
            case "appleskin": VBVAppleSkin.toggleEnabled(); break;
            case "crosshair": VBVCrosshairConfig.enabled = !VBVCrosshairConfig.enabled; break;
            case "entity-culling": VBVEntityCulling.toggle(); break;
            case "betterf3": VBVExtraFeatures.toggleBetterF3Enabled(); break;
            case "perf-meter": VBVPerf.toggleMeter(); break;
            default: return;
        }
        VBVConfigStore.saveAll();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(390, width - 24);
        int left = (width - panelWidth) / 2;
        int top = Math.max(24, height / 2 - 125);
        int rows = (MODULES.length + 1) / 2;
        int bottom = top + 65 + rows * 31 + 38;
        graphics.fill(left - 2, top - 2, left + panelWidth + 2, bottom + 2, 0xFF08060D);
        graphics.fill(left, top, left + panelWidth, bottom, 0xF20E0B16);
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFF9B55F5);
        graphics.fill(left, top + 2, left + panelWidth, top + 3, 0xFF512477);
        String title = "VEYDRIS CLIENT";
        graphics.text(font, title, width / 2 - font.width(title) / 2, top + 10, 0xFFE8D8FF, true);
        String hint = "Right Shift  •  Toggle modules  •  Settings save automatically";
        graphics.text(font, hint, width / 2 - font.width(hint) / 2, bottom - 15, 0xFF9C91AA, false);
    }

    @Override
    public void onClose() {
        VBVConfigStore.saveAll();
        VBVNav.open(null);
    }
}
