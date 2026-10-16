package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleMomentumOffsets extends LabelBundle {
    public LabelBundleMomentumOffsets() {

        // MM X OFFSET
        this.labels.add(new TextHUDElement("labelMMXOffset", "MM X Offset", "MM X Offset: ", new ScreenPosition(177, 110), false,
                () -> (ParkourTickListener.momentumBlock == null || ParkourTickListener.momentumBlock.lastOffsetX == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.momentumBlock.lastOffsetX),
                this::getDummyNumber));

        // MM Z OFFSET
        this.labels.add(new TextHUDElement("labelMMZOffset", "MM Z Offset", "MM Z Offset: ", new ScreenPosition(177, 119), false,
                () -> (ParkourTickListener.momentumBlock == null || ParkourTickListener.momentumBlock.lastOffsetZ == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.momentumBlock.lastOffsetZ),
                this::getDummyNumber));
    }
}