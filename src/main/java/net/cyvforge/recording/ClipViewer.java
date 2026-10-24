package net.cyvforge.recording;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.GuiHandler;
import net.cyvforge.event.events.MacroRecorder;
import net.cyvforge.gui.GuiClip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/** Only renders observations. It never changes player inputs, motion or position. */
public class ClipViewer {
    private static List<TickSnapshot> clip = Collections.emptyList();
    private static List<TickSnapshot> capturedClip = Collections.emptyList();
    private static int selected;
    private static int hovered = -1;
    private static String sourceName = "Clip";
    private static List<Integer> recordingTickNumbers = Collections.emptyList();
    private static boolean captureRequested;

    public static void requestCapture() {
        captureRequested = true;
    }

    public static void capture() {
        List<TickSnapshot> snapshot = MacroRecorder.clipBuffer.snapshot();
        if (snapshot.isEmpty()) {
            CyvForge.sendChatMessage("No ticks to clip yet.");
            return;
        }
        clip = snapshot;
        capturedClip = snapshot;
        sourceName = "Clip";
        recordingTickNumbers = Collections.emptyList();
        hovered = -1;
        selected = clip.size() - 1;
        CyvForge.sendChatMessage("Clipped " + clip.size() + " ticks (" + clip.size() / 20.0 + " seconds).");
        show();
    }

    public static void show() {
        if (capturedClip.isEmpty()) CyvForge.sendChatMessage("Capture a clip first.");
        else {
            clip = capturedClip;
            sourceName = "Clip";
            recordingTickNumbers = Collections.emptyList();
            selected = Math.min(selected, clip.size() - 1);
            hovered = -1;
            GuiHandler.setScreen(new GuiClip());
        }
    }

    public static void clear() {
        clip = Collections.emptyList();
        capturedClip = Collections.emptyList();
        selected = 0;
        hovered = -1;
        sourceName = "Clip";
        recordingTickNumbers = Collections.emptyList();
        captureRequested = false;
    }

    public static List<TickSnapshot> ticks() {
        return clip;
    }

    public static int selected() {
        return selected;
    }

    public static String sourceName() {
        return sourceName;
    }

    public static int tickNumber(int index) {
        return recordingTickNumbers.isEmpty() ? index + 1 : recordingTickNumbers.get(index);
    }

    public static List<String> tooltip(int index) {
        return clip.get(index).tooltip(sourceName, tickNumber(index));
    }

    /** Render saved observations after recording or loading the macro editor. */
    public static void showRecording(List<? extends List<String>> rows) {
        ArrayList<TickSnapshot> observed = new ArrayList<>();
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            TickSnapshot tick = TickSnapshot.fromMacroRow(rows.get(i));
            if (tick != null) {
                observed.add(tick);
                numbers.add(i + 1);
            }
        }
        clip = Collections.unmodifiableList(observed);
        recordingTickNumbers = Collections.unmodifiableList(numbers);
        sourceName = "Recording";
        selected = Math.max(0, clip.size() - 1);
        hovered = -1;
    }

    public static void select(int index) {
        selected = Math.max(0, Math.min(clip.size() - 1, index));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTick(TickEvent.ClientTickEvent event) {
        // Registered after the recorder: include the hotkey's completed tick.
        if (event.phase == TickEvent.Phase.END && captureRequested) {
            captureRequested = false;
            capture();
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity camera = mc.getRenderViewEntity();
        if (clip.isEmpty() || camera == null) return;
        Vec3 eye = camera.getPositionEyes(event.partialTicks);
        Vec3 look = camera.getLook(event.partialTicks);
        hovered = mc.currentScreen == null
                ? TickHover.find(clip, eye.xCoord, eye.yCoord, eye.zCoord, look.xCoord, look.yCoord, look.zCoord, 128)
                : -1;
        double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.partialTicks;
        double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.partialTicks;
        double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.partialTicks;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        // Raw GL state is restored as a unit, without disturbing GlStateManager's cache.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_LINE_BIT
                | GL11.GL_POINT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glLineWidth(2);
            renderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (TickSnapshot tick : clip) {
                renderer.pos(tick.x - cx, tick.y - cy + 0.02, tick.z - cz).color(0, 210, 255, 255).endVertex();
            }
            tessellator.draw();
            GL11.glPointSize(4);
            renderer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < clip.size(); i++) {
                TickSnapshot tick = clip.get(i);
                boolean highlighted = i == (hovered >= 0 ? hovered : selected);
                renderer.pos(tick.x - cx, tick.y - cy + 0.02, tick.z - cz)
                        .color(highlighted ? 255 : 0, 210, highlighted ? 0 : 255, 255).endVertex();
            }
            tessellator.draw();
            TickSnapshot tick = clip.get(hovered >= 0 ? hovered : selected);
            renderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            renderer.pos(tick.x - cx, tick.y - cy, tick.z - cz).color(255, 210, 0, 255).endVertex();
            renderer.pos(tick.x - cx, tick.y - cy + 1.8, tick.z - cz).color(255, 210, 0, 255).endVertex();
            tessellator.draw();
        } finally {
            GL11.glPopAttrib();
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || clip.isEmpty() || mc.currentScreen != null) return;
        int index = hovered >= 0 ? hovered : selected;
        List<String> lines = tooltip(index);
        ScaledResolution resolution = new ScaledResolution(mc);
        ArrayList<String> wrapped = new ArrayList<>();
        for (String line : lines) {
            wrapped.addAll(mc.fontRendererObj.listFormattedStringToWidth(line, resolution.getScaledWidth() - 16));
        }
        int width = 0;
        for (String line : wrapped) width = Math.max(width, mc.fontRendererObj.getStringWidth(line));
        int x = hovered >= 0 ? resolution.getScaledWidth() / 2 + 12 : 7;
        int y = hovered >= 0 ? resolution.getScaledHeight() / 2 + 12 : 7;
        x = Math.max(7, Math.min(x, resolution.getScaledWidth() - width - 7));
        y = Math.max(7, Math.min(y, resolution.getScaledHeight() - wrapped.size() * 10 - 7));
        Gui.drawRect(x - 4, y - 4, x + width + 4, y + wrapped.size() * 10 + 4, 0xC0000000);
        for (int i = 0; i < wrapped.size(); i++) mc.fontRendererObj.drawStringWithShadow(wrapped.get(i), x, y + i * 10, 0xFFFFFF);
    }
}
