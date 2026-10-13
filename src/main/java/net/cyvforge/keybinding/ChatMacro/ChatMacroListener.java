package net.cyvforge.keybinding.ChatMacro;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ChatMacroListener {

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null || mc.thePlayer == null) return;

        if (Keyboard.getEventKeyState()) {
            checkAndRunMacros(Keyboard.getEventKey());
        }
    }

    @SubscribeEvent
    public void onMouse(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null || mc.thePlayer == null) return;

        if (Mouse.getEventButtonState()) {
            int btn = Mouse.getEventButton();
            if (btn > 1) {
                checkAndRunMacros(1000 + btn);
            }
        }
    }

    private void checkAndRunMacros(int triggerKey) {
        for (ChatMacro hk : ChatMacroManager.hotkeys) {
            if (hk.keyCodes.contains(triggerKey)) {
                boolean allDown = true;
                for (int code : hk.keyCodes) {
                    if (code >= 1000) {
                        if (!Mouse.isButtonDown(code - 1000)) { allDown = false; break; }
                    } else {
                        if (!Keyboard.isKeyDown(code)) { allDown = false; break; }
                    }
                }

                if (allDown) {
                    if (hk.isChain) {
                        if (hk.isLink) {
                            executeCmd(hk.commandOn);
                            executeCmd(hk.commandOff);
                        } else {
                            hk.chainState = !hk.chainState;
                            executeCmd(hk.chainState ? hk.commandOn : hk.commandOff);
                        }
                    } else {
                        executeCmd(hk.commandOn);
                    }
                }
            }
        }
    }

    private void executeCmd(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        String trimmed = cmd.trim();

        String cmdName = trimmed.startsWith("/") ? trimmed.substring(1) : trimmed;
        cmdName = cmdName.split(" ")[0];

        boolean isClientCmd = ClientCommandHandler.instance.getCommands().containsKey(cmdName);

        if (isClientCmd) {
            String clientCmd = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, clientCmd);
        } else {
            mc.thePlayer.sendChatMessage(trimmed);
        }
    }
}