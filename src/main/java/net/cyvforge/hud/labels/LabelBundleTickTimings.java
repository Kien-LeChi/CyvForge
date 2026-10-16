package net.cyvforge.hud.labels;

import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.event.events.ParkourTickListener;
import net.cyvforge.hud.LabelBundle;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;

public class LabelBundleTickTimings extends LabelBundle {
    public LabelBundleTickTimings() {

        // LAST TIMING
        this.labels.add(new TextHUDElement("labelLastTiming", "Last Timing", "Last Timing: ", new ScreenPosition(0, 56), true,
                () -> ParkourTickListener.lastTiming, () -> "[Timing]"));

        // AIRTIME
        this.labels.add(new TextHUDElement("labelAirtime", "Airtime", "Airtime: ", new ScreenPosition(0, 65), true,
                () -> String.valueOf(ParkourTickListener.lastAirtime), () -> "0"));

        // STOPTIME
        this.labels.add(new TextHUDElement("labelStoptime", "Stoptime", "Stoptime: ", new ScreenPosition(0, 74), true,
                () -> String.valueOf(ParkourTickListener.lastStopTime), () -> "0"));

        // WAITTIME
        this.labels.add(new TextHUDElement("labelWaittime", "Waittime", "Waittime: ", new ScreenPosition(0, 83), true,
                () -> String.valueOf(ParkourTickListener.lastWaitTime), () -> "0"));

        // RUNTIME
        this.labels.add(new TextHUDElement("labelRuntime", "Runtime",
                () -> CyvClientConfig.getBoolean("hideRuntimeLabelName", false) ? "" : "Runtime: ",
                new ScreenPosition(0, 92), true,
                () -> {
                    int val = ParkourTickListener.lastRunTime;
                    if (val == 0 && CyvClientConfig.getBoolean("hideRuntimeIfZero", false)) return null;
                    return String.valueOf(val);
                }, () -> "0"));

        // TIER
        this.labels.add(new TextHUDElement("labelTier", "Tier", "Tier: ", new ScreenPosition(177, 137), false,
                () -> String.valueOf(12 - ParkourTickListener.lastAirtime), () -> "0"));
    }
}