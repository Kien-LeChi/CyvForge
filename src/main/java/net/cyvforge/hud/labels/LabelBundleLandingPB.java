package net.cyvforge.hud.labels;

import net.cyvforge.CyvForge;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleLandingPB extends LabelBundle {
    public LabelBundleLandingPB() {

        // PB
        this.labels.add(new TextHUDElement("labelPB", "PB", "PB: ", new ScreenPosition(106, 0), true,
                () -> (ParkourTickListener.landingBlock == null || ParkourTickListener.landingBlock.pb == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.landingBlock.pb),
                this::getDummyNumber));

        //  X OFFSET
        this.labels.add(new TextHUDElement("labelXOffset", "X Offset", "X Offset: ", new ScreenPosition(106, 9), true,
                () -> (ParkourTickListener.landingBlock == null || ParkourTickListener.landingBlock.lastOffsetX == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.landingBlock.lastOffsetX),
                this::getDummyNumber));

        // Z OFFSET
        this.labels.add(new TextHUDElement("labelZOffset", "Z Offset", "Z Offset: ", new ScreenPosition(106, 18), true,
                () -> (ParkourTickListener.landingBlock == null || ParkourTickListener.landingBlock.lastOffsetZ == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.landingBlock.lastOffsetZ),
                this::getDummyNumber));

        // TOTAL OFFSET
        this.labels.add(new TextHUDElement("labelTotalOffset", "Total Offset", "Total Offset: ", new ScreenPosition(106, 45), false,
                () -> (ParkourTickListener.landingBlock == null || ParkourTickListener.landingBlock.lastPb == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.landingBlock.lastPb),
                this::getDummyNumber));

        // X PB
        this.labels.add(new TextHUDElement("labelXPB", "X PB", "X PB: ", new ScreenPosition(106, 27), false,
                () -> (ParkourTickListener.landingBlock == null || ParkourTickListener.landingBlock.pbX == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.landingBlock.pbX),
                this::getDummyNumber));

        // Z PB
        this.labels.add(new TextHUDElement("labelZPB", "Z PB", "Z PB: ", new ScreenPosition(106, 36), false,
                () -> (ParkourTickListener.landingBlock == null || ParkourTickListener.landingBlock.pbZ == null) ? "NaN" : CyvForge.df.format(ParkourTickListener.landingBlock.pbZ),
                this::getDummyNumber));
    }
}