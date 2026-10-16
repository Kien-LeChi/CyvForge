package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovementInput;

public class LabelBundleLasts extends LabelBundle {
    public LabelBundleLasts() {

        // LAST 45
        this.labels.add(new TextHUDElement("labelLast45", "Last 45", "Last 45: ", new ScreenPosition(177, 74), false,
                () -> CyvForge.df.format(ParkourTickListener.last45), this::getDummyNumber));

        // LAST INPUT
        this.labels.add(new TextHUDElement("labelLastInput", "Last Input", "Last Input: ", new ScreenPosition(177, 92), false,
                () -> {
                    String str;
                    Minecraft mc = Minecraft.getMinecraft();
                    if (CyvClientConfig.getBoolean("WADdisplay", false)) {
                        str = (mc.gameSettings.keyBindForward.isKeyDown() ? "W" : "")
                                + (mc.gameSettings.keyBindLeft.isKeyDown() ? "A" : "")
                                + (mc.gameSettings.keyBindBack.isKeyDown() ? "S" : "")
                                + (mc.gameSettings.keyBindRight.isKeyDown() ? "D" : "");
                    } else {
                        MovementInput input = mc.thePlayer.movementInput;
                        str = (input.moveForward > 0 ? "W" : "")
                                + (input.moveStrafe > 0 ? "A" : "")
                                + (input.moveForward < 0 ? "S" : "")
                                + (input.moveStrafe < 0 ? "D" : "");
                    }
                    return str.isEmpty() ? "" : str;
                }, () -> "WASD"));

        // LAST TURNING
        this.labels.add(new TextHUDElement("labelLastTurningYaw", "Last Turning", "Last Turning: ", new ScreenPosition(177, 83), false,
                () -> CyvForge.df.format(ParkourTickListener.lastTurning) + "\u00B0",
                () -> getDummyNumber() + "\u00B0"));

        // LAST SIDESTEP
        this.labels.add(new TextHUDElement("labelLastSidestep", "Last Sidestep", "Last Sidestep: ", new ScreenPosition(177, 101), false,
                () -> {
                    if (ParkourTickListener.sidestep == 0) return "WAD " + ParkourTickListener.sidestepTime + " ticks";
                    else if (ParkourTickListener.sidestep == 1) return "WDWA";
                    else if (ParkourTickListener.sidestep == 2 && CyvClientConfig.getString("markDisplay", "Sidestep").equals("Sidestep")) return "Mark " + ParkourTickListener.sidestepTime + "t";
                    return "None";
                }, () -> "WAD"));
    }
}