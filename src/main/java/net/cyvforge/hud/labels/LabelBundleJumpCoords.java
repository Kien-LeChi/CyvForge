package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleJumpCoords extends LabelBundle {
    public LabelBundleJumpCoords() {

        // JUMP X
        this.labels.add(new TextHUDElement("labelJumpX", "Jump X", "Jump X: ", new ScreenPosition(0, 159), false,
                () -> CyvForge.df.format(ParkourTickListener.jx), this::getDummyNumber));

        // JUMP Y
        this.labels.add(new TextHUDElement("labelJumpY", "Jump Y", "Jump Y: ", new ScreenPosition(0, 168), false,
                () -> CyvForge.df.format(ParkourTickListener.jy), this::getDummyNumber));

        // JUMP Z
        this.labels.add(new TextHUDElement("labelJumpZ", "Jump Z", "Jump Z: ", new ScreenPosition(0, 177), false,
                () -> CyvForge.df.format(ParkourTickListener.jz), this::getDummyNumber));
    }
}