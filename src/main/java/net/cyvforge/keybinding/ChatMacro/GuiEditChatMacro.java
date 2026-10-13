package net.cyvforge.keybinding.ChatMacro;

import net.cyvforge.CyvForge;
import net.cyvforge.util.GuiUtils;
import net.cyvforge.util.defaults.CyvGui;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiEditChatMacro extends CyvGui {
    private final ChatMacro hk;
    private GuiTextField cmdOnField, cmdOffField;
    private SubButton doneBtn;
    private boolean listening = false;
    private boolean lastAnyKeyDown = false;

    private final int BORDER_COLOR = 0xFF555555;
    private final int ACTIVE_COLOR = 0xFFFFFF00;
    private final int ACTIVE_KEY_BG = 0x60FFFFFF;
    private final int INACTIVE_KEY_BG = 0x40000000;

    public GuiEditChatMacro(ChatMacro hk) {
        super("Edit Macro");
        this.hk = hk;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        int x = width / 2 - 100;
        int y = height / 2 - 60;

        cmdOnField = new GuiTextField(0, fontRendererObj, x, y, 200, 14);
        cmdOnField.setMaxStringLength(100);
        cmdOnField.setText(hk.commandOn);

        cmdOffField = new GuiTextField(1, fontRendererObj, x, y + 45, 200, 14);
        cmdOffField.setText(hk.commandOff);

        this.doneBtn = new SubButton("Done", width / 2 - 40, height / 2 + 85, 80, 16);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        super.drawDefaultBackground();
        int x = width / 2 - 110;
        int y = height / 2 - 85;

        int windowHeight = hk.isChain ? 220 : 150;
        GuiUtils.drawRoundedRect(x, y, x + 220, y + windowHeight, 5, CyvForge.theme.background1);

        String label1 = "Command:";
        if (hk.isChain) label1 = hk.isLink ? "Primary Command:" : "On Command:";
        fontRendererObj.drawString(label1, x + 10, y + 12, 0xFFFFFFFF);
        cmdOnField.drawTextBox();

        int dynamicOffset = hk.isChain ? 0 : -43;

        if (hk.isChain) {
            String label2 = hk.isLink ? "Secondary Command:" : "Off Command:";
            fontRendererObj.drawString(label2, x + 10, y + 55, 0xFFFFFFFF);
            cmdOffField.drawTextBox();
        }

        String keysDisplay;
        if (listening && hk.keyCodes.isEmpty()) {
            keysDisplay = "Press any keys/mouse...";
        } else {
            if (hk.keyCodes.isEmpty()) {
                keysDisplay = "NONE (Click to bind)";
            } else {
                List<String> names = new ArrayList<>();
                for (Integer code : hk.keyCodes) {
                    if (code >= 1000) {
                        names.add("Mouse" + (code - 1000 + 1));
                    } else {
                        names.add(Keyboard.getKeyName(code));
                    }
                }
                keysDisplay = String.join(" + ", names);

                if (keysDisplay.length() > 32) {
                    keysDisplay = keysDisplay.substring(0, 29) + "...";
                }
            }
        }

        int fieldY = y + 98 + dynamicOffset;
        GuiUtils.drawRoundedRect(x + 10, fieldY, x + 210, fieldY + 16, 2, listening ? ACTIVE_KEY_BG : INACTIVE_KEY_BG);
        GuiUtils.drawRectOutline(x + 10, fieldY, x + 210, fieldY + 16, BORDER_COLOR);
        fontRendererObj.drawString(keysDisplay, x + 15, fieldY + 4, listening ? ACTIVE_COLOR : 0xFFFFFFFF);

        int buttonsY = y + 128 + dynamicOffset;

        GuiUtils.drawRoundedRect(x + 10, buttonsY, x + 210, buttonsY + 15, 2, INACTIVE_KEY_BG);
        GuiUtils.drawRectOutline(x + 10, buttonsY, x + 210, buttonsY + 15, BORDER_COLOR);
        GuiUtils.drawCenteredString("Chain Mode: " + (hk.isChain ? "On" : "Off"), x + 110, buttonsY + 4, 0xFFFFFFFF, false);

        if (hk.isChain) {
            int modeY = buttonsY + 25;
            GuiUtils.drawRoundedRect(x + 10, modeY, x + 210, modeY + 15, 2, INACTIVE_KEY_BG);
            GuiUtils.drawRectOutline(x + 10, modeY, x + 210, modeY + 15, BORDER_COLOR);
            GuiUtils.drawCenteredString("Mode: " + (hk.isLink ? "Link" : "Step"), x + 110, modeY + 4, 0xFFFFFFFF, false);
        }

        int doneX = width / 2 - 40;
        int doneY = y + windowHeight - 25;
        boolean isHovered = mx >= doneX && mx <= doneX + 80 && my >= doneY && my <= doneY + 16;
        GuiUtils.drawRoundedRect(doneX, doneY, doneX + 80, doneY + 16, 3, isHovered ? 0xFF666666 : 0xFF444444);
        GuiUtils.drawCenteredString("Done", doneX + 40, doneY + 4, 0xFFFFFFFF, false);

        if (listening) {
            boolean anyDown = false;
            for (int code : hk.keyCodes) {
                if (code >= 1000) {
                    if (org.lwjgl.input.Mouse.isButtonDown(code - 1000)) {
                        anyDown = true;
                        break;
                    }
                } else {
                    if (org.lwjgl.input.Keyboard.isKeyDown(code)) {
                        anyDown = true;
                        break;
                    }
                }
            }
            lastAnyKeyDown = anyDown;
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        ChatMacroManager.save();
        super.onGuiClosed();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (listening) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) { listening = false; return; }
            if (!lastAnyKeyDown && !hk.keyCodes.isEmpty()) { hk.keyCodes.clear(); }
            if (!hk.keyCodes.contains(keyCode)) { hk.keyCodes.add(keyCode); }
            lastAnyKeyDown = true;
            return;
        }

        if (cmdOnField.isFocused()) cmdOnField.textboxKeyTyped(typedChar, keyCode);
        if (cmdOffField.isFocused()) cmdOffField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
            saveAndExit();
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        cmdOnField.mouseClicked(mx, my, btn);
        cmdOffField.mouseClicked(mx, my, btn);

        int x = width / 2 - 110;
        int y = height / 2 - 85;
        int dynamicOffset = hk.isChain ? 0 : -43;
        int fieldY = y + 98 + dynamicOffset;

        if (btn == 0 || btn == 1) {
            if (mx >= x + 10 && mx <= x + 210 && my >= fieldY && my <= fieldY + 16) {
                listening = true; hk.keyCodes.clear(); lastAnyKeyDown = false; return;
            } else {
                listening = false;
            }
        }

        if (listening && btn > 1) {
            int mouseId = 1000 + btn;

            if (!lastAnyKeyDown && !hk.keyCodes.isEmpty()) {
                hk.keyCodes.clear();
            }

            if (!hk.keyCodes.contains(mouseId)) {
                hk.keyCodes.add(mouseId);
            }
            lastAnyKeyDown = true;
            return;
        }

        if (btn == 0 || btn == 1) {
            int buttonsY = y + 128 + dynamicOffset;

            if (mx >= x + 10 && mx <= x + 210 && my >= buttonsY && my <= buttonsY + 15) {
                hk.isChain = !hk.isChain; return;
            }
            if (hk.isChain) {
                int modeY = buttonsY + 25;
                if (mx >= x + 10 && mx <= x + 210 && my >= modeY && my <= modeY + 15) {
                    hk.isLink = !hk.isLink; return;
                }
            }

            int windowHeight = hk.isChain ? 220 : 150;
            int doneX = width / 2 - 40;
            int doneY = y + windowHeight - 25;
            if (mx >= doneX && mx <= doneX + 80 && my >= doneY && my <= doneY + 16) {
                saveAndExit();
            }
        }
    }

    private void saveAndExit() {
        hk.commandOn = cmdOnField.getText();
        hk.commandOff = cmdOffField.getText();
        ChatMacroManager.save();
        mc.displayGuiScreen(new GuiChatMacro());
    }
}