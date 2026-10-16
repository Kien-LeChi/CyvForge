package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleSpeeds extends LabelBundle {
    public LabelBundleSpeeds() {

        // X SPEED
        this.labels.add(new TextHUDElement("labelXSpeed", "X Speed", "X Speed: ", new ScreenPosition(0, 187), false,
                () -> CyvForge.df.format(ParkourTickListener.vx), this::getDummyNumber));

        // Y SPEED
        this.labels.add(new TextHUDElement("labelYSpeed", "Y Speed", "Y Speed: ", new ScreenPosition(0, 196), false,
                () -> CyvForge.df.format(ParkourTickListener.vy), this::getDummyNumber));

        // Z SPEED
        this.labels.add(new TextHUDElement("labelZSpeed", "Z Speed", "Z Speed: ", new ScreenPosition(0, 205), false,
                () -> CyvForge.df.format(ParkourTickListener.vz), this::getDummyNumber));
    }
}