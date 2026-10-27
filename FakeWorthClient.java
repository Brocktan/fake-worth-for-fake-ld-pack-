package com.fakeworth;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client-side only. Rewrites the "$ <worth>" line of item tooltips and the
 * "~$ <total>" line of the /sell button. Nothing is sent to the server.
 */
public class FakeWorthClient implements ClientModInitializer {
    private static final Pattern NUMBER = Pattern.compile("\\d[\\d,]*(?:\\.\\d+)?\\s*[KkMmBbTt]?");

    private static FakeWorthConfig cfg;

    /** item id -> real worth per single item, learned from real tooltips you hover. */
    private static final Map<String, Double> REAL_UNIT = new HashMap<>();

    @Override
    public void onInitializeClient() {
        cfg = FakeWorthConfig.load();
        ItemTooltipCallback.EVENT.register(FakeWorthClient::onTooltip);
    }

    private static void onTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        if (cfg == null || !cfg.enabled) return;

        int worthLine = -1;
        boolean sellButton = false;
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).getString();
            String lower = s.toLowerCase();
            if (lower.contains("sell items")) sellButton = true;
            if (worthLine == -1 && s.contains("$") && !lower.contains("click")) worthLine = i;
        }
        if (worthLine == -1) return;

        Double realShown = WorthFormat.parse(lines.get(worthLine).getString().substring(
                lines.get(worthLine).getString().indexOf('$') + 1));

        if (sellButton) {
            if (realShown == null) return;
            Double total = fakeSellTotal(realShown);
            if (total == null) return;
            lines.set(worthLine, replaceNumber(lines.get(worthLine), WorthFormat.format(total)));
            return;
        }

        Double configured = cfg.valueFor(stack);
        if (configured == null) return;

        // Remember the real worth before we overwrite it (used for the sell button total).
        if (realShown != null && stack.getCount() > 0) {
            // the real tooltip shows the worth of the whole stack, so divide to get one item
            REAL_UNIT.put(FakeWorthConfig.id(stack), realShown / stack.getCount());
        }

        double shown = cfg.stackValue(configured, stack);
        lines.set(worthLine, replaceNumber(lines.get(worthLine), WorthFormat.format(shown)));
    }

    /** Total for the sell button: fake items count at their fake value. Returns null if no faked items are in the sell menu. */
    private static Double fakeSellTotal(double realTotal) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !(mc.currentScreen instanceof HandledScreen<?> screen)) return null;

        double fake = 0;
        double realOfFake = 0;
        boolean any = false;
        boolean allKnown = true;

        for (Slot slot : screen.getScreenHandler().slots) {
            if (slot.inventory == mc.player.getInventory()) continue; // only the sell container
            ItemStack s = slot.getStack();
            if (s.isEmpty()) continue;
            Double configured = cfg.valueFor(s);
            if (configured == null) continue;

            any = true;
            fake += cfg.stackValue(configured, s);
            Double unit = REAL_UNIT.get(FakeWorthConfig.id(s));
            if (unit == null) {
                allKnown = false;
            } else {
                realOfFake += unit * s.getCount();
            }
        }
        if (!any) return null;

        // If we never saw the real tooltip of a faked item, assume the whole real total came from them.
        double rest = allKnown ? Math.max(0, realTotal - realOfFake) : 0;
        return fake + rest;
    }

    /** Replaces the first number in the line, keeping every colour/style. */
    private static Text replaceNumber(Text line, String replacement) {
        MutableText out = Text.empty();
        boolean[] done = {false};
        line.<Boolean>visit((style, str) -> {
            Matcher m = NUMBER.matcher(str);
            if (!done[0] && m.find()) {
                done[0] = true;
                String r = str.substring(0, m.start()) + replacement + str.substring(m.end());
                out.append(Text.literal(r).setStyle(style));
            } else {
                out.append(Text.literal(str).setStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return out;
    }
}
