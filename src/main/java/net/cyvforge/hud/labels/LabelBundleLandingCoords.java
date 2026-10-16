package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleLandingCoords extends LabelBundle {
    public LabelBundleLandingCoords() {

        // LANDING X
        this.labels.add(new TextHUDElement("labelLandingX", "Last Landing X", "Last Landing X: ", new ScreenPosition(0, 75), true,
                () -> CyvForge.df.format(ParkourTickListener.lx), this::getDummyNumber));

        // LANDING Y
        this.labels.add(new TextHUDElement("labelLandingY", "Last Landing Y", "Last Landing Y: ", new ScreenPosition(0, 84), true,
                () -> CyvForge.df.format(ParkourTickListener.ly), this::getDummyNumber));

        // LANDING Z
        this.labels.add(new TextHUDElement("labelLandingZ", "Last Landing Z", "Last Landing Z: ", new ScreenPosition(0, 93), true,
                () -> CyvForge.df.format(ParkourTickListener.lz), this::getDummyNumber));
    }
}