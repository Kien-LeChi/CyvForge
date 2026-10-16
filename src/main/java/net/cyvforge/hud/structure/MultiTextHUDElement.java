package net.cyvforge.hud.structure;

import net.cyvforge.config.CyvClientColorHelper;
import java.util.function.Supplier;

public class MultiTextHUDElement extends DraggableHUDElement {
    private final String name;
    private final String displayName;
    private final ScreenPosition defaultPos;
    private final boolean enabledByDefault;
    private final Supplier<String[]> textPartsSupplier;
    private final Supplier<String[]> dummyPartsSupplier;

    public MultiTextHUDElement(String name, String displayName, ScreenPosition defaultPos, boolean enabledByDefault, Supplier<String[]> textPartsSupplier, Supplier<String[]> dummyPartsSupplier) {
        this.name = name;
        this.displayName = displayName;
        this.defaultPos = defaultPos;
        this.enabledByDefault = enabledByDefault;
        this.textPartsSupplier = textPartsSupplier;
        this.dummyPartsSupplier = dummyPartsSupplier;
    }

    @Override public String getName() { return name; }
    @Override public String getDisplayName() { return displayName; }
    @Override public ScreenPosition getDefaultPosition() { return defaultPos; }
    @Override public boolean enabledByDefault() { return enabledByDefault; }
    @Override public int getHeight() { return 9; }

    @Override
    public int getWidth() {
        String[] parts = dummyPartsSupplier != null ? dummyPartsSupplier.get() : textPartsSupplier.get();
        if (parts == null || parts.length == 0) return 0;
        int w = 0;
        for (String p : parts) if (p != null) w += mc.fontRendererObj.getStringWidth(p);
        return w;
    }

    @Override
    public void render(ScreenPosition pos) {
        if (!this.isVisible) return;
        renderParts(pos, textPartsSupplier.get());
    }

    @Override
    public void renderDummy(ScreenPosition pos) {
        renderParts(pos, dummyPartsSupplier != null ? dummyPartsSupplier.get() : textPartsSupplier.get());
    }

    private void renderParts(ScreenPosition pos, String[] parts) {
        if (parts == null || parts.length == 0) return;

        long c1 = CyvClientColorHelper.color1.getDrawColor();
        long c2 = CyvClientColorHelper.color2.getDrawColor();
        int currentX = pos.getAbsoluteX() + 1;
        int y = pos.getAbsoluteY() + 1;

        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null || parts[i].isEmpty()) continue;
            long color = (i % 2 == 0) ? c1 : c2;
            drawString(parts[i], currentX, y, color);
            currentX += mc.fontRendererObj.getStringWidth(parts[i]);
        }
    }
}