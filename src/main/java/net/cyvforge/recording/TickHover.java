package net.cyvforge.recording;

import java.util.List;

/** Ray picking against the visible tick markers; independent of movement physics. */
public final class TickHover {
    private static final double RADIUS = 0.14;

    private TickHover() { }

    public static int find(List<TickSnapshot> ticks, double eyeX, double eyeY, double eyeZ,
                           double lookX, double lookY, double lookZ, double range) {
        double length = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
        if (length == 0 || !Double.isFinite(length)) return -1;
        lookX /= length;
        lookY /= length;
        lookZ /= length;
        int hovered = -1;
        double nearest = range;
        for (int i = 0; i < ticks.size(); i++) {
            TickSnapshot tick = ticks.get(i);
            double dx = tick.x - eyeX, dy = tick.y + 0.02 - eyeY, dz = tick.z - eyeZ;
            double distance = dx * lookX + dy * lookY + dz * lookZ;
            if (distance < 0 || distance > range + RADIUS) continue;
            double offRayX = dx - distance * lookX;
            double offRayY = dy - distance * lookY;
            double offRayZ = dz - distance * lookZ;
            double perpendicular = offRayX * offRayX + offRayY * offRayY + offRayZ * offRayZ;
            if (perpendicular > RADIUS * RADIUS) continue;
            double hit = Math.max(0, distance - Math.sqrt(RADIUS * RADIUS - perpendicular));
            if (hit < nearest) {
                nearest = hit;
                hovered = i;
            }
        }
        return hovered;
    }
}
