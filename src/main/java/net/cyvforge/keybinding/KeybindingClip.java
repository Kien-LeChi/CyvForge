package net.cyvforge.keybinding;

import net.cyvforge.recording.ClipViewer;
import net.cyvforge.util.defaults.CyvKeybinding;
import org.lwjgl.input.Keyboard;

public class KeybindingClip extends CyvKeybinding {
    public KeybindingClip() {
        super("key.clip.desc", Keyboard.KEY_C);
    }

    @Override
    public void onTickEnd(boolean isPressed) {
        if (isPressed && client.currentScreen == null) ClipViewer.requestCapture();
    }
}
