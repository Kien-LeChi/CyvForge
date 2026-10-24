package net.cyvforge.recording;

import java.util.ArrayList;

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
