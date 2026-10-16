package net.cyvforge.event.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//This class is used to handle GUIs for CyvFabric
public class GuiHandler {
    private static GuiScreen screenAwaiting; //screen which will be shown next tick

    public static void setScreen(GuiScreen screen) {
        screenAwaiting = screen;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().theWorld == null) return; //don't run unless in-game
        if (event.phase != TickEvent.Phase.START) return;

        if (screenAwaiting != null) {
            Minecraft.getMinecraft().displayGuiScreen(screenAwaiting); //set the screen
            screenAwaiting = null; //now that no screen is awaiting, clear it
        }
    }

}
