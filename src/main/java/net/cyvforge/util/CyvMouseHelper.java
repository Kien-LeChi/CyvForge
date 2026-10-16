package net.cyvforge.util;

import net.cyvforge.config.CyvClientConfig;
import net.minecraft.util.MouseHelper;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.List;

public class CyvMouseHelper extends MouseHelper {

    private final List<Mouse> mice = new ArrayList<>();

    public CyvMouseHelper() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (Controller controller : controllers) {
            if (controller.getType() == Controller.Type.MOUSE) {
                mice.add((Mouse) controller);
            }
        }
    }

    @Override
    public void grabMouseCursor() {
        super.grabMouseCursor();

        if (CyvClientConfig.getBoolean("rawInput", false)) {
            for (Mouse m : mice) {
                m.poll();
                m.getX().getPollData();
                m.getY().getPollData();
            }
        }
    }

    @Override
    public void mouseXYChange() {
        int lwjglDx = org.lwjgl.input.Mouse.getDX();
        int lwjglDy = org.lwjgl.input.Mouse.getDY();

        if (CyvClientConfig.getBoolean("rawInput", false) && Display.isActive()) {

            int rawDx = 0;
            int rawDy = 0;

            for (Mouse m : mice) {
                m.poll();
                rawDx += (int) m.getX().getPollData();
                rawDy += (int) m.getY().getPollData();
            }

            if (org.lwjgl.input.Mouse.isGrabbed()) {
                this.deltaX = rawDx;
                this.deltaY = -rawDy;
            } else {
                this.deltaX = 0;
                this.deltaY = 0;
            }

        } else {
            this.deltaX = lwjglDx;
            this.deltaY = lwjglDy;
        }
    }
}