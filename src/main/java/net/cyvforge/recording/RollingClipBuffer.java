package net.cyvforge.recording;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RollingClipBuffer {
    public static final int DEFAULT_SECONDS = 10;
    public static final int MAX_SECONDS = 600;
    private final ArrayDeque<TickSnapshot> ticks = new ArrayDeque<>();

    public static int ticksForSeconds(int seconds) {
        return Math.max(1, Math.min(MAX_SECONDS, seconds)) * 20;
    }

    public void add(TickSnapshot tick, int capacity) {
        ticks.addLast(tick);
        resize(capacity);
    }

    public void resize(int capacity) {
        while (ticks.size() > Math.max(1, capacity)) ticks.removeFirst();
    }

    public List<TickSnapshot> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(ticks));
    }

    public void clear() {
        ticks.clear();
    }
}
