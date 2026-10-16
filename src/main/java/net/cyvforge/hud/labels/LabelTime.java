package net.cyvforge.hud.labels;

import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.hud.structure.TextHUDElement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LabelTime extends TextHUDElement {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    // TIME
    public LabelTime() {
        super("ModSystemTime", "System Time Label", "Time: ", new ScreenPosition(106, 54), false,
                () -> dtf.format(LocalDateTime.now()),
                () -> "23:59:59");
    }
}