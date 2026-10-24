package net.cyvforge.recording;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.cyvforge.gui.GuiClip;
import java.util.List;

/** Executable regression tests; run with gradlew recordingTest. */
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
        verifyHoverVelocity(sprintReleased, "Clip");
        TickSnapshot recorded = TickSnapshot.fromMacroRow(restored);
        require(recorded != null && !recorded.keyDown(5) && recorded.sprinting,
                "Recording hover must load the physical key and actual sprint state");
        verifyHoverVelocity(recorded, "Recording");
        require(TickSnapshot.fromMacroRow(row.subList(0, 10)) == null,
                "Legacy recordings must not invent zero velocities");
        List<String> corrupt = new java.util.ArrayList<>(restored);
        corrupt.set(13, "not a velocity");
        require(TickSnapshot.fromMacroRow(corrupt) == null, "Corrupt observations must not crash the viewer");

        List<TickSnapshot> markers = java.util.Arrays.asList(
                new TickSnapshot(keys, false, 0, 0, 5, 0, 0, .001, -.002, .003),
                new TickSnapshot(keys, false, 0, 0, 2, 0, 0, .004, -.003, .002));
        require(TickHover.find(markers, 0, .02, 0, 0, 0, 1, 128) == 1,
                "World hover must choose the nearest tick marker, not the selected tick");
        require(TickHover.find(markers, 1, .02, 0, 0, 0, 1, 128) == -1,
                "Looking beside a marker must not show its hover tooltip");
        require(TickHover.find(markers, 0, .02, 0, 0, 0, -1, 128) == -1,
                "Markers behind the camera must not hover");
        require(TickHover.find(markers, 0, .02, 0, 0, 0, 1, 1) == -1,
                "Markers beyond the hover range must not hover");
        require(TickHover.find(markers, 0, .02, 0, 0, 0, 0, 128) == -1,
                "A zero-length look direction must not select a marker");
        ClipViewer.showRecording(java.util.Arrays.asList(row.subList(0, 10), restored, corrupt));
        require(ClipViewer.ticks().size() == 1, "Only rows with stored observations can become world markers");
        require(ClipViewer.tooltip(0).get(0).equals("Recording tick 2"),
                "World tooltips must retain recording tick numbers when legacy/manual rows have no marker");
        require(ClipViewer.tooltip(0).contains("Raw VY: -0.003"),
                "The actual world tooltip entry point must expose stored raw velocity");
        require(TickHover.find(ClipViewer.ticks(), 1, 64.02, -5, 0, 0, 1, 128) == 0,
                "Loaded recording observations must be hoverable in the world");
        ClipViewer.showRecording(java.util.Arrays.asList(restored, tick(2, keys, false, .75, .5, .25).toMacroRow(0, 0)));
        GuiClip inspector = new GuiClip();
        inspector.width = 640;
        inspector.height = 480;
        require(ClipViewer.selected() == 1 && inspector.hoverTooltip(12, 153).contains("Raw VZ: 1.0E-12"),
                "Row hover must show the pointed-at tick's velocity even when another tick is selected");
        require(inspector.hoverTooltip(12, 175).contains("Raw VX: 0.75"), "Hover must follow the mouse to the next row");
        require(inspector.hoverTooltip(12, 144).isEmpty() && inspector.hoverTooltip(639, 153).isEmpty(),
                "Hovering outside the visible rows must not show tick information");
        ClipViewer.clear();

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

    private static void verifyHoverVelocity(TickSnapshot tick, String source) {
        List<String> tooltip = tick.tooltip(source, 7);
        require(tooltip.get(0).equals(source + " tick 7"), "Tooltip must identify its own tick");
        require(tooltip.contains("Raw VX: 0.004999999999"), source + " hover rounded or clamped X");
        require(tooltip.contains("Raw VY: -0.003"), source + " hover rounded or clamped Y");
        require(tooltip.contains("Raw VZ: 1.0E-12"), source + " hover rounded or clamped Z");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
