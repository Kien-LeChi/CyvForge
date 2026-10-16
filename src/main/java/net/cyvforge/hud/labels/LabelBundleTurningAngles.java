package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleTurningAngles extends LabelBundle {
    public LabelBundleTurningAngles() {

        // JUMP ANGLE
        this.labels.add(new TextHUDElement("labelJumpAngle", "Jump Angle", "Jump Angle: ", new ScreenPosition(0, 103), true,
                () -> CyvForge.df.format(ParkourTickListener.formatYaw(ParkourTickListener.jf)) + "\u00B0",
                () -> getDummyNumber() + "\u00B0"));

        // SECOND TURN
        this.labels.add(new TextHUDElement("labelSecondTurn", "Second Turn", "Second Turn: ", new ScreenPosition(0, 112), true,
                () -> CyvForge.df.format(ParkourTickListener.formatYaw(ParkourTickListener.turningAngles[1])) + "\u00B0",
                () -> getDummyNumber() + "\u00B0"));

        // PRETURN
        this.labels.add(new TextHUDElement("labelPreturn", "Preturn", "Preturn: ", new ScreenPosition(0, 121), true,
                () -> CyvForge.df.format(ParkourTickListener.formatYaw(ParkourTickListener.pf)) + "\u00B0",
                () -> getDummyNumber() + "\u00B0"));
    }
}