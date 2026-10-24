package net.cyvforge.recording;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

/** Dependency-free regression tests; run with gradlew recordingTest. */
public class RecordingTest {
    public static void main(String[] args) {
        boolean[] keys = new boolean[8];
        keys[0] = true;
        TickSnapshot sprintReleased = tick(1, keys, true, 0.004999999999, -0.003, 1.0E-12);
        List<String> row = sprintReleased.toMacroRow(0, 0);
        require(Boolean.parseBoolean(row.get(5)), "Releasing sprint must preserve active sprint in replay");
        require(!sprintReleased.keyDown(5), "Physical sprint key must still show released");
        require(!Boolean.parseBoolean(tick(2, keys, false, 0, 0, 0).toMacroRow(0, 0).get(5)),
                "A sprint stopped by Minecraft must stay stopped");
        keys[5] = true;
        require(Boolean.parseBoolean(tick(3, keys, false, 0, 0, 0).toMacroRow(0, 0).get(5)),
                "The first sprint key tick must still initiate sprint");
        require(!sprintReleased.keyDown(5), "Snapshots must own their inputs");
        require(Double.parseDouble(row.get(13)) == 0.004999999999, "Small X velocity was clamped");
        require(Double.parseDouble(row.get(14)) == -0.003, "Small Y velocity was clamped");
        require(Double.parseDouble(row.get(15)) == 1.0E-12, "Tiny Z velocity was rounded away");
        require(Double.parseDouble(row.get(10)) == 1, "Position must be stored directly");
        require(row.get(16).equals("false") && row.get(17).equals("true"),
                "Macro metadata must distinguish key release from active sprint");
        List<String> restored = new Gson().fromJson(new Gson().toJson(row), new TypeToken<List<String>>() {}.getType());
        require(restored.equals(row), "Saved macro must preserve inputs and full-precision observations");

        RollingClipBuffer buffer = new RollingClipBuffer();
        for (int i = 1; i <= 205; i++) buffer.add(tick(i, keys, false, 0, 0, 0), 200);
        List<TickSnapshot> clip = buffer.snapshot();
        require(clip.size() == 200 && clip.get(0).x == 6 && clip.get(199).x == 205,
                "Default clip must contain the latest 200 ordered ticks");
        buffer.resize(20);
        require(buffer.snapshot().get(0).x == 186, "Shrinking must keep the newest ticks");
        buffer.add(tick(206, keys, false, 0, 0, 0), 40);
        require(buffer.snapshot().size() == 21, "Growing the buffer must retain existing history without inventing ticks");
        require(clip.size() == 200 && clip.get(199).x == 205, "Captured clips must stay frozen");
        buffer.clear();
        require(buffer.snapshot().isEmpty(), "Disconnect/world reset must clear history");
        require(clip.size() == 200, "Reset must not mutate a captured clip");
        try {
            clip.clear();
            throw new AssertionError("Captured clip must be immutable");
        } catch (UnsupportedOperationException expected) { }
        require(RollingClipBuffer.ticksForSeconds(10) == 200, "10 seconds must be 200 game ticks");
        require(RollingClipBuffer.ticksForSeconds(-1) == 20, "Invalid config must be bounded");
        require(RollingClipBuffer.ticksForSeconds(Integer.MAX_VALUE) == 12000, "Huge config must be bounded");
        System.out.println("Recording regression tests passed.");
    }

    private static TickSnapshot tick(double x, boolean[] keys, boolean sprinting, double vx, double vy, double vz) {
        return new TickSnapshot(keys, sprinting, x, 64, -x, 0, 0, vx, vy, vz);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
