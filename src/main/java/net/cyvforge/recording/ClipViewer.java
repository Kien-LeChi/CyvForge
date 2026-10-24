package net.cyvforge.recording;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.GuiHandler;
import net.cyvforge.event.events.MacroRecorder;
import net.cyvforge.gui.GuiClip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

/** Only renders observations. It never changes player inputs, motion or position. */
public class ClipViewer {
    private static List<TickSnapshot> clip = Collections.emptyList();
    private static int selected;
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
        selected = clip.size() - 1;
        CyvForge.sendChatMessage("Clipped " + clip.size() + " ticks (" + clip.size() / 20.0 + " seconds).");
        show();
    }

    public static void show() {
        if (clip.isEmpty()) CyvForge.sendChatMessage("Capture a clip first.");
        else GuiHandler.setScreen(new GuiClip());
    }

    public static void clear() {
        clip = Collections.emptyList();
        selected = 0;
        captureRequested = false;
    }

    public static List<TickSnapshot> ticks() {
        return clip;
    }

    public static int selected() {
        return selected;
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
                renderer.pos(tick.x - cx, tick.y - cy + 0.02, tick.z - cz)
                        .color(i == selected ? 255 : 0, 210, i == selected ? 0 : 255, 255).endVertex();
            }
            tessellator.draw();
            TickSnapshot tick = clip.get(selected);
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
        TickSnapshot tick = clip.get(selected);
        String[] lines = {"Clip tick " + (selected + 1) + "/" + clip.size() + " | /cyv clip show",
                tick.inputs() + " | Sprinting: " + tick.sprinting,
                "X: " + tick.x, "Y: " + tick.y, "Z: " + tick.z,
                "Raw VX: " + tick.vx, "Raw VY: " + tick.vy, "Raw VZ: " + tick.vz};
        int width = 0;
        for (String line : lines) width = Math.max(width, mc.fontRendererObj.getStringWidth(line));
        Gui.drawRect(3, 3, width + 11, lines.length * 10 + 8, 0xA0000000);
        for (int i = 0; i < lines.length; i++) mc.fontRendererObj.drawStringWithShadow(lines[i], 7, 7 + i * 10, 0xFFFFFF);
    }
}
