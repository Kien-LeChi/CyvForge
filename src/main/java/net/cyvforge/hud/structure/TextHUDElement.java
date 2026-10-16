package net.cyvforge.hud.structure;

import net.cyvforge.config.CyvClientColorHelper;
import net.minecraft.client.Minecraft;
import java.util.function.Supplier;

public class TextHUDElement extends DraggableHUDElement {
    private final String name;
    private final String displayName;
    private final Supplier<String> prefixSupplier;
    private final ScreenPosition defaultPos;
    private final boolean enabledByDefault;
    private final Supplier<String> valueSupplier;
    private final Supplier<String> dummyValueSupplier;

    public TextHUDElement(String name, String displayName, String prefix, ScreenPosition defaultPos, boolean enabledByDefault, Supplier<String> valueSupplier, Supplier<String> dummyValueSupplier) {
        this(name, displayName, () -> prefix, defaultPos, enabledByDefault, valueSupplier, dummyValueSupplier);
    }

    public TextHUDElement(String name, String displayName, Supplier<String> prefixSupplier, ScreenPosition defaultPos, boolean enabledByDefault, Supplier<String> valueSupplier, Supplier<String> dummyValueSupplier) {
        this.name = name;
        this.displayName = displayName;
        this.prefixSupplier = prefixSupplier;
        this.defaultPos = defaultPos;
        this.enabledByDefault = enabledByDefault;
        this.valueSupplier = valueSupplier;
        this.dummyValueSupplier = dummyValueSupplier;
    }

    @Override public String getName() { return name; }
    @Override public String getDisplayName() { return displayName; }
    @Override public ScreenPosition getDefaultPosition() { return defaultPos; }
    @Override public boolean enabledByDefault() { return enabledByDefault; }

    @Override
    public int getWidth() {
        String val = dummyValueSupplier != null ? dummyValueSupplier.get() : valueSupplier.get();
        if (val == null) return 0;
        return mc.fontRendererObj.getStringWidth(prefixSupplier.get() + val);
    }

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    public void render(ScreenPosition pos) {
        if (!this.isVisible) return;
        String value = valueSupplier.get();
        if (value == null) return;

        String prefix = prefixSupplier.get();
        long color1 = CyvClientColorHelper.color1.getDrawColor();
        long color2 = CyvClientColorHelper.color2.getDrawColor();

        drawString(prefix, pos.getAbsoluteX() + 1, pos.getAbsoluteY() + 1, color1);
        drawString(value, pos.getAbsoluteX() + 1 + mc.fontRendererObj.getStringWidth(prefix), pos.getAbsoluteY() + 1, color2);
    }

    @Override
    public void renderDummy(ScreenPosition pos) {
        String value = dummyValueSupplier != null ? dummyValueSupplier.get() : valueSupplier.get();
        if (value == null) return;

        String prefix = prefixSupplier.get();
        long color1 = CyvClientColorHelper.color1.getDrawColor();
        long color2 = CyvClientColorHelper.color2.getDrawColor();

        drawString(prefix, pos.getAbsoluteX() + 1, pos.getAbsoluteY() + 1, color1);
        drawString(value, pos.getAbsoluteX() + 1 + mc.fontRendererObj.getStringWidth(prefix), pos.getAbsoluteY() + 1, color2);
    }
}