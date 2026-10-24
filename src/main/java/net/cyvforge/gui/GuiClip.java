package net.cyvforge.gui;

import net.cyvforge.CyvForge;
import net.cyvforge.recording.ClipViewer;
import net.cyvforge.recording.TickSnapshot;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.Collections;

/** Read-only tick inspector. Closing it leaves the observed path visible. */
public class GuiClip extends GuiScreen {
    private static final int LIST_TOP = 151;
    private static final int ROW_HEIGHT = 22;
    private int firstRow;

    @Override
    public void initGui() {
        keepSelectedVisible();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xC0101720);
        List<TickSnapshot> ticks = ClipViewer.ticks();
        if (ticks.isEmpty()) {
            drawCenteredString(fontRendererObj, "Clip cleared. Press Esc to return.", width / 2, 20, 0xFFFFFF);
            return;
        }
        TickSnapshot tick = ticks.get(ClipViewer.selected());
        drawCenteredString(fontRendererObj, ClipViewer.sourceName() + " - " + ticks.size() + " ticks / " + ticks.size() / 20.0 + "s", width / 2, 10, 0x50DCFF);
        String[] details = {"Tick " + ClipViewer.tickNumber(ClipViewer.selected()) + ": " + tick.inputs(),
                "Sprinting: " + tick.sprinting + " | Sprint key: " + tick.keyDown(5),
                "X: " + tick.x, "Y: " + tick.y, "Z: " + tick.z,
                "Raw VX: " + tick.vx, "Raw VY: " + tick.vy, "Raw VZ: " + tick.vz,
                Float.isNaN(tick.yaw) ? "Absolute yaw/pitch not stored in this recording."
                        : "Yaw: " + tick.yaw + " | Pitch: " + tick.pitch,
                "Velocity is incoming motion before Minecraft's cutoff."};
        for (int i = 0; i < details.length; i++) fontRendererObj.drawString(details[i], 12, 28 + i * 11, 0xFFFFFF);

        for (int i = firstRow; i < Math.min(ticks.size(), firstRow + visibleRows()); i++) {
            int top = LIST_TOP + (i - firstRow) * ROW_HEIGHT;
            TickSnapshot row = ticks.get(i);
            drawRect(8, top, width - 8, top + ROW_HEIGHT - 1,
                    i == ClipViewer.selected() ? 0xB0406170 : 0x80505050);
            fontRendererObj.drawString("Tick " + ClipViewer.tickNumber(i) + " | " + row.inputs(), 12, top + 2, 0xFFFFFF);
            String position = "XYZ: " + CyvForge.df.format(row.x) + ", " + CyvForge.df.format(row.y) + ", " + CyvForge.df.format(row.z);
            fontRendererObj.drawString(position, 12, top + 12, 0xBBBBBB);
        }
        drawCenteredString(fontRendererObj, "Click / scroll / arrows: select tick | Esc: view path", width / 2, height - 14, 0xCCCCCC);
        List<String> tooltip = hoverTooltip(mouseX, mouseY);
        if (!tooltip.isEmpty()) drawHoveringText(tooltip, mouseX, mouseY);
    }

    public List<String> hoverTooltip(int mouseX, int mouseY) {
        if (mouseX < 8 || mouseX >= width - 8 || mouseY < LIST_TOP
                || mouseY >= LIST_TOP + visibleRows() * ROW_HEIGHT
                || (mouseY - LIST_TOP) % ROW_HEIGHT == ROW_HEIGHT - 1) return Collections.emptyList();
        int index = firstRow + (mouseY - LIST_TOP) / ROW_HEIGHT;
        return index < ClipViewer.ticks().size() ? ClipViewer.tooltip(index) : Collections.emptyList();
    }

    private int visibleRows() {
        return Math.max(1, (height - LIST_TOP - 24) / ROW_HEIGHT);
    }

    private void keepSelectedVisible() {
        if (ClipViewer.selected() < firstRow) firstRow = ClipViewer.selected();
        if (ClipViewer.selected() >= firstRow + visibleRows()) firstRow = ClipViewer.selected() - visibleRows() + 1;
        firstRow = Math.max(0, firstRow);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == 0 && mouseX >= 8 && mouseX < width - 8
                && mouseY >= LIST_TOP && mouseY < LIST_TOP + visibleRows() * ROW_HEIGHT) {
            ClipViewer.select(firstRow + (mouseY - LIST_TOP) / ROW_HEIGHT);
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            ClipViewer.select(ClipViewer.selected() - Integer.signum(wheel));
            keepSelectedVisible();
        }
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        if (key == Keyboard.KEY_UP || key == Keyboard.KEY_LEFT) ClipViewer.select(ClipViewer.selected() - 1);
        else if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_RIGHT) ClipViewer.select(ClipViewer.selected() + 1);
        else if (key == Keyboard.KEY_PRIOR) ClipViewer.select(ClipViewer.selected() - visibleRows());
        else if (key == Keyboard.KEY_NEXT) ClipViewer.select(ClipViewer.selected() + visibleRows());
        else if (key == Keyboard.KEY_HOME) ClipViewer.select(0);
        else if (key == Keyboard.KEY_END) ClipViewer.select(ClipViewer.ticks().size() - 1);
        else super.keyTyped(character, key);
        keepSelectedVisible();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
