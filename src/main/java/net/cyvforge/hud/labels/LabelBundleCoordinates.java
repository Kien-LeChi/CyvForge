package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;
import net.cyvforge.hud.structure.MultiTextHUDElement;
import net.minecraft.client.Minecraft;

public class LabelBundleCoordinates extends LabelBundle {
    public LabelBundleCoordinates() {

        // FPS
        this.labels.add(new TextHUDElement("labelFPS", "FPS", "FPS: ", new ScreenPosition(0, 1), true,
                () -> "" + Minecraft.getDebugFPS(), () -> "360"));

        // X
        this.labels.add(new TextHUDElement("labelX", "X Coord", "X: ", new ScreenPosition(0, 10), true,
                () -> CyvForge.df.format(CyvClientConfig.getBoolean("frameBased", false) ? Minecraft.getMinecraft().getRenderViewEntity().posX : ParkourTickListener.x),
                this::getDummyNumber));

        // Y
        this.labels.add(new TextHUDElement("labelY", "Y Coord", "Y: ", new ScreenPosition(0, 19), true,
                () -> CyvForge.df.format(CyvClientConfig.getBoolean("frameBased", false) ? Minecraft.getMinecraft().getRenderViewEntity().posY : ParkourTickListener.y),
                this::getDummyNumber));

        // Z
        this.labels.add(new TextHUDElement("labelZ", "Z Coord", "Z: ", new ScreenPosition(0, 28), true,
                () -> CyvForge.df.format(CyvClientConfig.getBoolean("frameBased", false) ? Minecraft.getMinecraft().getRenderViewEntity().posZ : ParkourTickListener.z),
                this::getDummyNumber));

        // YAW
        this.labels.add(new MultiTextHUDElement("labelYaw", "Yaw", new ScreenPosition(0, 37), true,
                () -> {
                    float yaw = CyvClientConfig.getBoolean("frameBased", false) ? Minecraft.getMinecraft().getRenderViewEntity().rotationYaw : ((ParkourTickListener.lastTick == null) ? 0 : ParkourTickListener.lastTick.f);
                    float fYaw = ParkourTickListener.formatYaw(yaw);
                    String axis = CyvClientConfig.getBoolean("showFacingAxis", false) ? ((Math.abs(fYaw) > 45 && Math.abs(fYaw) < 135) ? " X" : " Z") : "";

                    return new String[] { "F: ", CyvForge.df.format(fYaw) + "\u00B0", axis };
                },
                () -> {
                    String axis = CyvClientConfig.getBoolean("showFacingAxis", false) ? " Z" : "";
                    return new String[] { "F: ", getDummyNumber() + "\u00B0", axis };
                }
        ));

        // PITCH
        this.labels.add(new TextHUDElement("labelPitch", "Pitch", "Pitch: ", new ScreenPosition(0, 46), true,
                () -> {
                    float pitch = CyvClientConfig.getBoolean("frameBased", false) ? Minecraft.getMinecraft().getRenderViewEntity().rotationPitch : ((ParkourTickListener.lastTick == null) ? 0 : ParkourTickListener.lastTick.p);
                    return CyvForge.df.format(pitch) + "\u00B0";
                }, () -> getDummyNumber() + "\u00B0"));
    }
}