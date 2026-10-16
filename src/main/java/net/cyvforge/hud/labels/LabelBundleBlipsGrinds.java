package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.MultiTextHUDElement;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleBlipsGrinds extends LabelBundle {
    public LabelBundleBlipsGrinds() {

        // BLIPS
        this.labels.add(new MultiTextHUDElement("labelBlips", "Blips", new ScreenPosition(177, 164), false,
                () -> {
                    if (CyvClientConfig.getBoolean("simpleBlip", false))
                        return new String[] { "Blips: ", String.valueOf(ParkourTickListener.blips) };

                    return new String[] { "Blips: ", String.valueOf(ParkourTickListener.blips), " chained / Y: ", CyvForge.df.format(ParkourTickListener.lastBlipHeight) };
                },
                () -> {
                    if (CyvClientConfig.getBoolean("simpleBlip", false))
                        return new String[] { "Blips: ", "0" };

                    return new String[] { "Blips: ", "0", " chained / Y: ", getDummyNumber() };
                }
        ));

        // GRINDS
        this.labels.add(new TextHUDElement("labelGrinds", "Grinds", "Grinds: ", new ScreenPosition(177, 173), false,
                () -> String.valueOf(ParkourTickListener.grinds),
                () -> "0"
        ));
    }
}