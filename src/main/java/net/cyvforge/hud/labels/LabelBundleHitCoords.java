package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleHitCoords extends LabelBundle {
    public LabelBundleHitCoords() {

        // HIT X
        this.labels.add(new TextHUDElement("labelHitX", "Hit X", "Hit X: ", new ScreenPosition(0, 131), true,
                () -> CyvForge.df.format(ParkourTickListener.hx), this::getDummyNumber));

        // HIT Y
        this.labels.add(new TextHUDElement("labelHitY", "Hit Y", "Hit Y: ", new ScreenPosition(0, 140), true,
                () -> CyvForge.df.format(ParkourTickListener.hy), this::getDummyNumber));

        // HIT Z
        this.labels.add(new TextHUDElement("labelHitZ", "Hit Z", "Hit Z: ", new ScreenPosition(0, 149), true,
                () -> CyvForge.df.format(ParkourTickListener.hz), this::getDummyNumber));
    }
}