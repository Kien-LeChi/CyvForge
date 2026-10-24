package net.cyvforge.event.events;

import net.cyvforge.keybinding.*;
import net.cyvforge.util.defaults.CyvKeybinding;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class KeyInputHandler {
    public static ArrayList<CyvKeybinding> cyvKeybindings = new ArrayList<CyvKeybinding>();

    public KeyInputHandler() {
        cyvKeybindings.add(new KeybindingHUDPositions());
        cyvKeybindings.add(new KeybindingMPKGui());
        cyvKeybindings.add(new KeybindingTogglesprint());
        cyvKeybindings.add(new KeybindingRunMacro());
        cyvKeybindings.add(new KeybindingStopMacro());
        cyvKeybindings.add(new KeybindingClip());

        for (CyvKeybinding k : cyvKeybindings) ClientRegistry.registerKeyBinding(k);
    }

    @SubscribeEvent
    public void onEvent(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().theWorld == null) return;

        if (event.phase == TickEvent.Phase.START) {
            for (CyvKeybinding k : cyvKeybindings) {
                k.onTickStart(k.isPressed());
            }
        }

        if (event.phase == TickEvent.Phase.END) {
            for (CyvKeybinding k : cyvKeybindings) {
                k.onTickEnd(k.isPressed());
            }
        }
    }



}
