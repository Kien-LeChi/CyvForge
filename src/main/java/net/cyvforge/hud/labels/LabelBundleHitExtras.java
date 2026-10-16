package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.MultiTextHUDElement;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleHitExtras extends LabelBundle {
    public LabelBundleHitExtras() {

        // HIT ANGLE
        this.labels.add(new TextHUDElement("labelHitAngle", "Hit Angle", "Hit Angle: ", new ScreenPosition(177, 146), false,
                () -> CyvForge.df.format(ParkourTickListener.formatYaw(ParkourTickListener.hf)) + "\u00B0",
                () -> getDummyNumber() + "\u00B0"));

        // HIT VECTOR
        this.labels.add(new MultiTextHUDElement("labelHitVector", "Hit Vector", new ScreenPosition(177, 155), false,
                () -> {
                    String speed = CyvForge.df.format(Math.hypot(ParkourTickListener.hvx, ParkourTickListener.hvz));
                    String angle = CyvForge.df.format(Math.toDegrees(Math.atan2((ParkourTickListener.hvx == 0) ? 0 : -ParkourTickListener.hvx, ParkourTickListener.hvz)));

                    return new String[] { "Hit Vector: ", speed, "/", angle + "\u00B0" };
                },
                () -> new String[] { "Hit Vector: ", getDummyNumber(), "/", getDummyNumber() + "\u00B0" }
        ));
    }
}