package net.cyvforge.keybinding.ChatMacro;

import net.cyvforge.CyvForge;
import net.cyvforge.gui.GuiMPK;
import net.cyvforge.util.GuiUtils;
import net.cyvforge.util.defaults.CyvGui;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiChatMacro extends CyvGui {
    int sizeX = 250;
    int sizeY = 200;
    SubButton addNewBtn;
    SubButton backBtn;

    float scroll = 0;
    float vScroll = 0;
    int maxScroll = 0;

    public GuiChatMacro() {
        super("Command Hotkeys");
    }

    @Override
    public void initGui() {
        int x = width / 2 - sizeX / 2;
        int y = height / 2 - sizeY / 2;

        this.backBtn = new SubButton("Back", x, y - 25, 40, 14);
        this.addNewBtn = new SubButton("Add New Hotkey", width / 2 - 50, y + sizeY - 20, 100, 16);

        this.backBtn.setEnabled(true);
        this.addNewBtn.setEnabled(true);

        ChatMacroManager.load();
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        super.drawDefaultBackground();

        int x = width / 2 - sizeX / 2;
        int y = height / 2 - sizeY / 2;

        maxScroll = Math.max(0, (ChatMacroManager.hotkeys.size() * 22) - (sizeY - 65));

        int bx = x;
        int by = y - 25;
        int bw = 40;
        int bh = 14;
        boolean backHover = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;

        int backColor = backHover ? 0xBF333333 : 0xBF000000;
        GuiUtils.drawRoundedRect(bx, by, bx + bw, by + bh, 5, backColor);
        GuiUtils.drawCenteredString("Back", bx + bw/2, by + 3, 0xFFFFFFFF, false);

        GuiUtils.drawRoundedRect(x - 5, y - 5, x + sizeX + 5, y + sizeY + 5, 5, CyvForge.theme.background1);
        GuiUtils.drawCenteredString("Command Hotkeys", width / 2, y + 5, 0xFFFFFFFF, true);

        int scale = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, (height - (y + sizeY - 25)) * scale, sizeX * scale, (sizeY - 55) * scale);

        for (int i = 0; i < ChatMacroManager.hotkeys.size(); i++) {
            ChatMacro hk = ChatMacroManager.hotkeys.get(i);
            int rowY = (int) (y + 35 + (i * 22) - scroll);

            String keyName;
            if (hk.keyCodes == null || hk.keyCodes.isEmpty()) {
                keyName = "NONE";
            } else {
                List<String> names = new ArrayList<>();
                for (Integer code : hk.keyCodes) {
                    if (code >= 1000) {
                        names.add("Mouse" + (code - 1000 + 1));
                    } else {
                        names.add(Keyboard.getKeyName(code));
                    }
                }
                keyName = String.join("+", names);
                if (keyName.length() > 16) keyName = keyName.substring(0, 13) + "...";
            }

            String cmdTxt = hk.commandOn.isEmpty() ? "Empty" : hk.commandOn;
            if (cmdTxt.length() > 18) cmdTxt = cmdTxt.substring(0, 15) + "...";

            int rowColor = (mx >= x + 5 && mx <= x + sizeX - 5 && my >= rowY && my <= rowY + 20) ? 0x60FFFFFF : 0x20FFFFFF;
            GuiUtils.drawRoundedRect(x + 5, rowY, x + sizeX - 5, rowY + 20, 2, rowColor);

            String mainText = (i + 1) + ": " + cmdTxt + " [" + keyName + "]";
            fontRendererObj.drawString(mainText, x + 10, rowY + 6, 0xFFFFFFFF);
            fontRendererObj.drawString("X", x + sizeX - 18, rowY + 6, 0xFFFF5555);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (maxScroll > 0) {
            int barHeight = sizeY - 60;
            int scrollIndicatorHeight = Math.max(10, (int) ((float) barHeight * barHeight / (maxScroll + barHeight)));
            int scrollIndicatorPos = (int) (scroll / maxScroll * (barHeight - scrollIndicatorHeight));
            Gui.drawRect(x - 3, y + 35, x - 1, y + 35 + barHeight, 0x30FFFFFF);
            Gui.drawRect(x - 3, y + 35 + scrollIndicatorPos, x - 1, y + 35 + scrollIndicatorPos + scrollIndicatorHeight, 0xFFFFFFFF);
        }

        addNewBtn.draw(mx, my);
    }

    @Override
    public void updateScreen() {
        scroll += vScroll;
        vScroll *= 0.7f;
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int eventDWheel = org.lwjgl.input.Mouse.getEventDWheel();

        if (eventDWheel != 0 && !org.lwjgl.input.Mouse.isButtonDown(0)) {
            vScroll -= eventDWheel * 0.05f;
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int x = width / 2 - sizeX / 2;
        int y = height / 2 - sizeY / 2;

        if (mx >= x && mx <= x + 40 && my >= y - 25 && my <= y - 11) {
            mc.displayGuiScreen(new GuiMPK());
            return;
        }

        if (addNewBtn.clicked(mx, my, btn)) {
            ChatMacro newHk = new ChatMacro();
            ChatMacroManager.hotkeys.add(newHk);
            mc.displayGuiScreen(new GuiEditChatMacro(newHk));
            return;
        }

        for (int i = 0; i < ChatMacroManager.hotkeys.size(); i++) {
            int rowY = (int) (y + 35 + (i * 22) - scroll);
            if (my >= y + 35 && my <= y + sizeY - 30) {
                if (mx >= x + 5 && mx <= x + sizeX - 5 && my >= rowY && my <= rowY + 20) {
                    if (mx >= x + sizeX - 25) {
                        ChatMacroManager.hotkeys.remove(i);
                        ChatMacroManager.save();
                    } else {
                        mc.displayGuiScreen(new GuiEditChatMacro(ChatMacroManager.hotkeys.get(i)));
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new GuiMPK());
        }
    }
}