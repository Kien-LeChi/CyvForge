package net.cyvforge.hud.nonlabels;

import net.cyvforge.hud.structure.MultiTextHUDElement;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.keybinding.KeybindingTogglesprint;

public class TogglesprintHUD extends MultiTextHUDElement {

    public TogglesprintHUD() {
        super("togglesprintHUD", "Togglesprint HUD", new ScreenPosition(0, 232), true,
                () -> KeybindingTogglesprint.sprintToggled ? new String[] { "[", "Sprint Toggled", "]" } : null,
                () -> new String[] { "[", "Sprint Toggled", "]" }
        );
    }

}