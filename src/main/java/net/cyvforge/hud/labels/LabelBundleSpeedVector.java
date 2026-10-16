package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.MultiTextHUDElement;
import net.cyvforge.hud.structure.ScreenPosition;

public class LabelBundleSpeedVector extends LabelBundle {
    public LabelBundleSpeedVector() {

        // SPEEDS
        this.labels.add(new MultiTextHUDElement("labelIndividualSpeeds", "Speeds", new ScreenPosition(0, 214), false,
                () -> new String[] {
                        "Speeds: ", CyvForge.df.format(ParkourTickListener.vx),
                        "/", CyvForge.df.format(ParkourTickListener.vy),
                        "/", CyvForge.df.format(ParkourTickListener.vz)
                },
                () -> new String[] {
                        "Speeds: ", getDummyNumber(),
                        "/", getDummyNumber(),
                        "/", getDummyNumber()
                }
        ));

        // SPEED VECTOR
        this.labels.add(new MultiTextHUDElement("labelSpeedVector", "Speed Vector", new ScreenPosition(0, 223), false,
                () -> {
                    String speed = CyvForge.df.format(Math.hypot(ParkourTickListener.vx, ParkourTickListener.vz));
                    String angle = CyvForge.df.format(Math.toDegrees(Math.atan2(ParkourTickListener.vx == 0 ? 0 : -ParkourTickListener.vx, ParkourTickListener.vz)));

                    return new String[] { "Speed Vector: ", speed, "/", angle + "\u00B0" };
                },
                () -> new String[] {
                        "Speed Vector: ", getDummyNumber(), "/", getDummyNumber() + "\u00B0"
                }
        ));
    }
}