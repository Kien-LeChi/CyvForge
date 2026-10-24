package net.cyvforge.recording;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Observed position and incoming (pre-cutoff) motion, never simulated. */
public final class TickSnapshot {
    private final boolean[] keys;
    public final boolean sprinting;
    public final double x, y, z, vx, vy, vz;
    public final float yaw, pitch;

    public TickSnapshot(boolean[] keys, boolean sprinting, double x, double y, double z,
                        float yaw, float pitch, double vx, double vy, double vz) {
        this.keys = keys.clone();
        this.sprinting = sprinting;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public boolean keyDown(int index) {
        return keys[index];
    }

    public String inputs() {
        String[] names = {"W", "A", "S", "D", "Jump", "Sprint key", "Sneak", "RMB"};
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i]) text.append(names[i]).append(' ');
        }
        return text.length() == 0 ? "None" : text.toString().trim();
    }

    /** Shared by row tooltips and world-marker hover for both data sources. */
    public List<String> tooltip(String source, int tickNumber) {
        return Arrays.asList(source + " tick " + tickNumber, "Inputs: " + inputs(),
                "X: " + x, "Y: " + y, "Z: " + z,
                "Raw VX: " + vx, "Raw VY: " + vy, "Raw VZ: " + vz,
                "Sprinting: " + sprinting + " | Sprint key: " + keyDown(5));
    }

    /** Legacy input-only rows have no observed velocity or position. */
    public static TickSnapshot fromMacroRow(List<String> row) {
        if (row == null || row.size() < 18) return null;
        try {
            boolean[] keys = new boolean[8];
            for (int i = 0; i < keys.length; i++) keys[i] = Boolean.parseBoolean(row.get(i));
            keys[5] = Boolean.parseBoolean(row.get(16));
            // Macro yaw/pitch fields are turn deltas, not absolute view angles.
            return new TickSnapshot(keys, Boolean.parseBoolean(row.get(17)),
                    Double.parseDouble(row.get(10)), Double.parseDouble(row.get(11)), Double.parseDouble(row.get(12)),
                    Float.NaN, Float.NaN,
                    Double.parseDouble(row.get(13)), Double.parseDouble(row.get(14)), Double.parseDouble(row.get(15)));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public ArrayList<String> toMacroRow(float yawDelta, float pitchDelta) {
        ArrayList<String> row = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            // Sprint is latched by vanilla; key release alone never cancels it.
            row.add(Boolean.toString(i == 5 ? keys[i] || sprinting : keys[i]));
        }
        row.add(Float.toString(yawDelta));
        row.add(Float.toString(pitchDelta));
        row.add(Double.toString(x));
        row.add(Double.toString(y));
        row.add(Double.toString(z));
        row.add(Double.toString(vx));
        row.add(Double.toString(vy));
        row.add(Double.toString(vz));
        row.add(Boolean.toString(keys[5]));
        row.add(Boolean.toString(sprinting));
        return row;
    }
}
