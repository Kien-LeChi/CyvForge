package net.cyvforge.gui;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.cyvforge.CyvForge;
import net.cyvforge.command.mpk.CommandMacro;
import net.cyvforge.config.ColorTheme;
import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.event.MacroFileInit;
import net.cyvforge.event.events.GuiHandler;
import net.cyvforge.util.defaults.CyvGui;
import net.cyvforge.util.GuiUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;

public class GuiMacro extends CyvGui {
    int sizeX = 500;
    int sizeY = 300;

    SubButton addRow;
    SubButton duplicateRow;
    SubButton deleteRow;

    GuiTextField fileName;
    SubButton loadFile;

    public ArrayList<MacroLine> macroLines;
    int selectedIndex = -1;

    float vScroll = 0;
    float scroll = 0;
    int maxScroll = 0;
    boolean scrollClicked = false;

    public GuiMacro() {
        super("Macro GUI");
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        mc.displayGuiScreen(null);
    }

    @Override
    public void initGui() { //initialize the macro
        ArrayList<ArrayList<String>> macro;
        this.macroLines = new ArrayList<>();

        Keyboard.enableRepeatEvents(true);

        this.addRow = new SubButton("Add Row", (sr.getScaledWidth() + sizeY +30)/2 - 75, sr.getScaledHeight()/2 - sizeY/2 + 40);
        this.duplicateRow = new SubButton("Duplicate Row", (sr.getScaledWidth() + sizeY +30)/2 - 75, sr.getScaledHeight()/2 - sizeY/2 + 60);
        this.deleteRow = new SubButton("Delete Row", (sr.getScaledWidth() + sizeY +30)/2 - 75, sr.getScaledHeight()/2 - sizeY/2 + 80);

        this.fileName = new GuiTextField(0, fontRendererObj, (sr.getScaledWidth() + sizeY +30)/2 - 72, sr.getScaledHeight()/2 - sizeY/2+fontRendererObj.FONT_HEIGHT/2 + 20, 90, fontRendererObj.FONT_HEIGHT*2);
        fileName.setEnableBackgroundDrawing(false);
        fileName.setText(CyvClientConfig.getString("currentMacro", "macro"));
        this.loadFile = new SubButton("Load", (sr.getScaledWidth() + sizeY +30)/2 + 25, sr.getScaledHeight()/2 - sizeY/2 + 20);
        loadFile.setSizeX(50);
        loadFile.setEnabled(true);

        this.macroLines.clear();
        try {
            MacroFileInit.swapFile(CyvClientConfig.getString("currentMacro", "macro"));
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(MacroFileInit.macroFile));
            macro = gson.fromJson(reader, new ArrayList<ArrayList<String>>().getClass());
        } catch (Exception e) {
            macro = new ArrayList<ArrayList<String>>();
        }

        if (macro != null) {
            try {
                for (ArrayList<String> line : macro) {
                    try {
                        MacroLine macroLine = new MacroLine();

                        macroLine.w = Boolean.valueOf(line.get(0));
                        macroLine.a = Boolean.valueOf(line.get(1));
                        macroLine.s = Boolean.valueOf(line.get(2));
                        macroLine.d = Boolean.valueOf(line.get(3));
                        macroLine.jump = Boolean.valueOf(line.get(4));
                        macroLine.sprint = Boolean.valueOf(line.get(5));
                        macroLine.sneak = Boolean.valueOf(line.get(6));
                        //macroLine.rmb = Boolean.valueOf(line.get(7));

                        //macroLine.yawField.setText(""+Double.valueOf(line.get(8)));
                        //macroLine.pitchField.setText(""+Double.valueOf(line.get(9)));

                        // transisition fix for when i added RMB
                        if (line.size() == 9) {
                            macroLine.rmb = false;
                            macroLine.yawField.setText("" + Double.valueOf(line.get(7)));
                            macroLine.pitchField.setText("" + Double.valueOf(line.get(8)));
                        } else {
                            macroLine.rmb = Boolean.valueOf(line.get(7));
                            macroLine.yawField.setText("" + Double.valueOf(line.get(8)));
                            macroLine.pitchField.setText("" + Double.valueOf(line.get(9)));
                        }

                        if (line.size() > 10) macroLine.observations.addAll(line.subList(10, line.size()));

                        macroLines.add(macroLine);

                    } catch (Exception e) {}
                }
            } catch (Exception e) {}
        }

        maxScroll = (int) Math.max(0, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 2 * Math.ceil(macroLines.size()) - (sizeY-20));
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();

        maxScroll = (int) Math.max(0, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 2 * Math.ceil(macroLines.size()) - (sizeY-20));
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;

        GuiUtils.drawRoundedRect(sr.getScaledWidth()/2 - sizeX/2 - 15, sr.getScaledHeight()/2 - sizeY/2 - 4,
                sr.getScaledWidth()/2 + sizeX/2 + 14, sr.getScaledHeight()/2 + sizeY/2 + 4, 5, CyvForge.theme.background1);

        this.addRow.draw(mouseX, mouseY);
        this.duplicateRow.draw(mouseX, mouseY);
        this.deleteRow.draw(mouseX, mouseY);

        if (selectedIndex >= 0 && selectedIndex < macroLines.size()) {
            MacroLine selected = macroLines.get(selectedIndex);
            int detailX = (sr.getScaledWidth() + sizeY + 30) / 2 - 72;
            int detailY = sr.getScaledHeight() / 2 - sizeY / 2 + 108;
            GuiUtils.drawString("Tick " + (selectedIndex + 1) + " (recorded)", detailX, detailY, 0xFFFFFFFF);
            String[] labels = {"X", "Y", "Z", "Raw VX", "Raw VY", "Raw VZ", "Sprint key", "Sprinting"};
            if (selected.observations.size() >= labels.length) {
                for (int i = 0; i < labels.length; i++) {
                    String text = labels[i] + ": " + selected.observations.get(i);
                    fontRendererObj.drawSplitString(text, detailX, detailY + 14 + i * 21, 142, 0xFFFFFFFF);
                }
            } else {
                fontRendererObj.drawSplitString("No position or velocity data in this legacy/manual row.", detailX, detailY + 14, 142, 0xFFBBBBBB);
            }
        }

        GuiUtils.drawRoundedRect((sr.getScaledWidth() + sizeY +30)/2 - 75, sr.getScaledHeight()/2 - sizeY/2 + 20,
                (sr.getScaledWidth() + sizeY +30)/2 + 20, sr.getScaledHeight()/2 - sizeY/2 + 20 + fontRendererObj.FONT_HEIGHT*7/4,
                3, CyvForge.theme.shade2);
        this.fileName.drawTextBox();
        this.loadFile.draw(mouseX, mouseY);

        this.addRow.setEnabled(true);
        if (this.selectedIndex > -1 && this.selectedIndex < this.macroLines.size()) {
            this.duplicateRow.setEnabled(true);
            this.deleteRow.setEnabled(true);
        } else {
            this.duplicateRow.setEnabled(false);
            this.deleteRow.setEnabled(false);
        }


        int centerx = sr.getScaledWidth() * sr.getScaleFactor() / 2;
        int centery = sr.getScaledHeight() * sr.getScaleFactor() / 2;
        int scaleFactor = sr.getScaleFactor();

        GuiUtils.drawString("Inputs:", sr.getScaledWidth()/2 - sizeX/2 + 13, 5 + sr.getScaledHeight()/2 - sizeY/2, 0xFFFFFFFF, true);
        GuiUtils.drawString("Yaw:", sr.getScaledWidth()/2 - 34, 5 + sr.getScaledHeight()/2 - sizeY/2, 0xFFFFFFFF, true);
        GuiUtils.drawString("Pitch:", sr.getScaledWidth()/2 - 1, 5 + sr.getScaledHeight()/2 - sizeY/2, 0xFFFFFFFF, true);

        GL11.glScissor(centerx - ((sizeX + 20)*scaleFactor/2),
                centery - (sizeY*scaleFactor/2) + 3,
                sizeX*scaleFactor, sizeY*scaleFactor - (fontRendererObj.FONT_HEIGHT * scaleFactor * 2));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int index = 0;
        for (MacroLine l : macroLines) {
            int yHeight = (int) ((index + 1) * mc.fontRendererObj.FONT_HEIGHT*2 - scroll + (sr.getScaledHeight()/2 - sizeY/2));
            GuiUtils.drawString(""+(index+1), sr.getScaledWidth()/2 - sizeX/2 - 10,
                    yHeight + fontRendererObj.FONT_HEIGHT*2/3, 0xFFFFFFFF);
            l.drawEntry(index, (int) scroll, mouseX, mouseY, index == this.selectedIndex);
            index++;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //draw scrollbar
        int scrollbarHeight = (int) ((sizeY - 8)/(0.01*maxScroll+1));
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0) scroll = 0;

        int top = sr.getScaledHeight()/2-sizeY/2+4;
        int bottom = sr.getScaledHeight()/2+sizeY/2-4 - scrollbarHeight;
        int amount = (int) (top + (bottom - top) * ((float) scroll/maxScroll));

        if (maxScroll == 0) amount = top;

        //color
        int color = CyvForge.theme.border2;
        if (mouseX > sr.getScaledWidth()/2+sizeX/2+2 && mouseX < sr.getScaledWidth()/2+sizeX/2+8 &&
                mouseY > amount && mouseY < amount+scrollbarHeight) {
            color = CyvForge.theme.border1;
        }

        GuiUtils.drawRoundedRect(sr.getScaledWidth()/2+sizeX/2+2, amount,
                sr.getScaledWidth()/2+sizeX/2+8, amount+scrollbarHeight, 3, color);

    }

    @Override
    public void handleMouseInput() throws IOException {
        int eventDWheel = GuiHandler.scrollBuffer;
        GuiHandler.scrollBuffer = 0;

        if (eventDWheel != 0 && (!scrollClicked || !org.lwjgl.input.Mouse.isButtonDown(0))) {
            vScroll -= eventDWheel * 0.05;
        }

        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseEvent) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseEvent);

        int scrollbarHeight = (int) ((sizeY - 8)/(0.01*maxScroll+1));
        int top = sr.getScaledHeight()/2-sizeY/2+4;
        int bottom = sr.getScaledHeight()/2+sizeY/2-4 - scrollbarHeight;
        int amount = (int) (top + (bottom - top) * ((float) scroll/maxScroll));

        if (mouseX > sr.getScaledWidth()/2+sizeX/2+2 && mouseX < sr.getScaledWidth()/2+sizeX/2+8 &&
                mouseY > amount && mouseY < amount+scrollbarHeight) {
            this.scrollClicked = true;
            return;
        } else {
            this.scrollClicked = false;
        }

        this.fileName.mouseClicked(mouseX, mouseY, mouseEvent);

        int index=0;
        for (MacroLine l : macroLines) {
            if (l.isPressed(index, mouseX, mouseY, mouseEvent)) {

                if (this.selectedIndex == index) {
                    if (!l.pitchField.isFocused() && !l.yawField.isFocused()) {
                        int keyCode = mouseEvent - 100;
                        if (keyCode == mc.gameSettings.keyBindForward.getKeyCode()) {
                            l.w = !l.w;
                        } else if (keyCode == mc.gameSettings.keyBindLeft.getKeyCode()) {
                            l.a = !l.a;
                        } else if (keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
                            l.s = !l.s;
                        } else if (keyCode == mc.gameSettings.keyBindRight.getKeyCode()) {
                            l.d = !l.d;
                        } else if (keyCode == mc.gameSettings.keyBindJump.getKeyCode()) {
                            l.jump = !l.jump;
                        } else if (keyCode == mc.gameSettings.keyBindSprint.getKeyCode()) {
                            l.sprint = !l.sprint;
                        } else if (keyCode == mc.gameSettings.keyBindSneak.getKeyCode()) {
                            l.sneak = !l.sneak;
                        } else if (keyCode == mc.gameSettings.keyBindUseItem.getKeyCode()) {
                            l.rmb = !l.rmb;
                        }
                    }
                } else {
                    this.selectedIndex = index;

                }
                l.mouseClicked(index, mouseX, mouseY, mouseEvent);
                return;
            }
            index++;
        }

        if (this.addRow.clicked(mouseX, mouseY, mouseEvent)) {
            try {
                if (!(this.selectedIndex > -1 && this.selectedIndex < this.macroLines.size())) {
                    this.macroLines.add(new MacroLine());
                    maxScroll = (int) Math.max(0, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 2 * Math.ceil(macroLines.size()) - (sizeY-20));
                    this.scroll = this.maxScroll;
                    this.selectedIndex = this.macroLines.size()-1;
                } else {
                    this.macroLines.add(selectedIndex+1, new MacroLine());
                }
            } catch (Exception e) {e.printStackTrace();}
        } else if (this.duplicateRow.clicked(mouseX, mouseY, mouseEvent)) {
            try {
                MacroLine oldLine = this.macroLines.get(this.selectedIndex);
                MacroLine newLine = new MacroLine();

                newLine.w = oldLine.w;
                newLine.a = oldLine.a;
                newLine.s = oldLine.s;
                newLine.d = oldLine.d;
                newLine.jump = oldLine.jump;
                newLine.sprint = oldLine.sprint;
                newLine.sneak = oldLine.sneak;
                newLine.rmb = oldLine.rmb;
                newLine.observations.addAll(oldLine.observations);

                newLine.yawField.setText(oldLine.yawField.getText());
                newLine.pitchField.setText(oldLine.pitchField.getText());

                this.macroLines.add(selectedIndex, newLine);
            } catch (Exception e) {}
        } else if (this.deleteRow.clicked(mouseX, mouseY, mouseEvent)) {
            try {
                this.macroLines.remove(selectedIndex);
            } catch (Exception e) {}
        } else if (this.loadFile.clicked(mouseX, mouseY, mouseEvent)) {
            CyvClientConfig.set("currentMacro", this.fileName.getText());
            mc.displayGuiScreen(new GuiMacro());
        }

    }

    @Override
    public void mouseClickMove(int x, int y, int mouseButton, long time) {
        if (this.scrollClicked) {
            int scrollbarHeight = (int) ((sizeY - 8)/(0.01*maxScroll+1));
            int top = sr.getScaledHeight()/2-sizeY/2+4;
            int bottom = sr.getScaledHeight()/2+sizeY/2-4 - scrollbarHeight;

            scroll = (int) ((float) (y - (sr.getScaledHeight()/2-this.sizeY/2) - scrollbarHeight/2) /(bottom - top) * maxScroll);

            if (scroll > maxScroll) scroll = maxScroll;
            if (scroll < 0) scroll = 0;
        }

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (this.fileName.isFocused()) {
            this.fileName.textboxKeyTyped(typedChar, keyCode);
            return;
        }

        if (this.selectedIndex > -1 && this.selectedIndex < macroLines.size()) {
            MacroLine l = this.macroLines.get(this.selectedIndex);

            if (l.yawField.isFocused()) l.yawField.textboxKeyTyped(typedChar, keyCode);
            else if (l.pitchField.isFocused()) l.pitchField.textboxKeyTyped(typedChar, keyCode);
            else {
                if (keyCode == mc.gameSettings.keyBindForward.getKeyCode()) {
                    l.w = !l.w;
                } else if (keyCode == mc.gameSettings.keyBindLeft.getKeyCode()) {
                    l.a = !l.a;
                } else if (keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
                    l.s = !l.s;
                } else if (keyCode == mc.gameSettings.keyBindRight.getKeyCode()) {
                    l.d = !l.d;
                } else if (keyCode == mc.gameSettings.keyBindJump.getKeyCode()) {
                    l.jump = !l.jump;
                } else if (keyCode == mc.gameSettings.keyBindSprint.getKeyCode()) {
                    l.sprint = !l.sprint;
                } else if (keyCode == mc.gameSettings.keyBindSneak.getKeyCode()) {
                    l.sneak = !l.sneak;
                } else if (keyCode == mc.gameSettings.keyBindUseItem.getKeyCode()) {
                    l.rmb = !l.rmb;
                }
            }

        }
    }

    @Override
    public void updateScreen() {
        if (this.selectedIndex > -1 && this.selectedIndex < macroLines.size()) {
            MacroLine l = this.macroLines.get(this.selectedIndex);

            l.yawField.updateCursorCounter();
            l.pitchField.updateCursorCounter();
        }

        this.fileName.updateCursorCounter();

        //smooth scrolling
        this.scroll += this.vScroll;
        this.vScroll *= 0.75;

        if (this.fileName.getText().length() < 1 || this.fileName.getText().length() > 32) this.loadFile.setEnabled(false);
        else if (this.fileName.getText().equals(CyvClientConfig.getString("currentMacro", "macro"))) this.loadFile.setEnabled(false);
        else this.loadFile.setEnabled(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();

        //save macro
        try {
            FileWriter fileWriter = new FileWriter(MacroFileInit.macroFile, false);
            Gson gson = new Gson();
            ArrayList<ArrayList<String>> macroList = new ArrayList<ArrayList<String>>();

            fileWriter.write("[" + System.getProperty("line.separator"));

            for (MacroLine line : this.macroLines) {
                ArrayList<String> macroString = new ArrayList<String>();
                macroString.add(line.w ? "true" : "false");
                macroString.add(line.a ? "true" : "false");
                macroString.add(line.s ? "true" : "false");
                macroString.add(line.d ? "true" : "false");
                macroString.add(line.jump ? "true" : "false");
                macroString.add(line.sprint ? "true" : "false");
                macroString.add(line.sneak ? "true" : "false");
                macroString.add(line.rmb ? "true" : "false");

                try {
                    macroString.add(Double.parseDouble(line.yawField.getText()) + "");
                } catch (NumberFormatException e) {
                    macroString.add("0.0");
                }

                try {
                    macroString.add(Double.parseDouble(line.pitchField.getText()) + "");
                } catch (NumberFormatException e) {
                    macroString.add("0.0");
                }

                macroString.addAll(line.observations);
                macroList.add(macroString);
                fileWriter.write(gson.toJson(macroString) + (macroLines.indexOf(line) == macroLines.size()-1 ? "" : ",") + System.getProperty("line.separator"));
            }

            fileWriter.write("]");

            CommandMacro.macro = macroList;
            String json = gson.toJson(macroList.toArray());
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MacroLine {
        int xStart = sr.getScaledWidth()/2 - sizeX/2 + 10;
        int width = sizeX/2 + 20;
        int height = mc.fontRendererObj.FONT_HEIGHT*2;

        public boolean w, a, s, d, jump, sprint, sneak, rmb;
        public ArrayList<String> observations = new ArrayList<>();

        GuiTextField yawField, pitchField;

        public MacroLine() {
            this.yawField = new GuiTextField(7, fontRendererObj, 0, 0, 33, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
            this.pitchField = new GuiTextField(8, fontRendererObj, 0, 0, 33, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
            this.yawField.setEnableBackgroundDrawing(false);
            this.pitchField.setEnableBackgroundDrawing(false);

            this.yawField.setText("0.0");
            this.pitchField.setText("0.0");
        }

        public void drawEntry(int slotIndex, int scroll, int mouseX, int mouseY, boolean isSelected) {
            int yHeight = (slotIndex + 1) * mc.fontRendererObj.FONT_HEIGHT*2 - scroll + (sr.getScaledHeight()/2 - sizeY/2);
            GuiUtils.drawRoundedRect(xStart, yHeight + 1,
                    xStart + width, yHeight + height - 1,
                    3, isSelected ? CyvForge.theme.shade1 : CyvForge.theme.shade2);

            StringBuilder string = new StringBuilder();
            if (w) string.append("W ");
            if (a) string.append("A ");
            if (s) string.append("S ");
            if (d) string.append("D ");
            if (jump) string.append("Jump ");
            if (sprint) string.append("Spr ");
            if (sneak) string.append("Snk ");
            if (rmb) string.append("RMB ");

            GuiUtils.drawString(string.toString(), xStart + 4, yHeight + height/3, 0xFFFFFFFF);

            this.yawField.yPosition = yHeight + mc.fontRendererObj.FONT_HEIGHT*3/4;
            this.pitchField.yPosition = yHeight + mc.fontRendererObj.FONT_HEIGHT*3/4;
            this.yawField.xPosition = sr.getScaledWidth()/2 - 42;
            this.pitchField.xPosition = sr.getScaledWidth()/2 - 5;

            GuiUtils.drawRoundedRect(sr.getScaledWidth()/2 - 46, yHeight + 2,
                    sr.getScaledWidth()/2 - 41+31, yHeight + mc.fontRendererObj.FONT_HEIGHT*2 - 2,
                    2, CyvForge.theme.highlight);
            GuiUtils.drawRoundedRect(sr.getScaledWidth()/2 - 8, yHeight + 2,
                    sr.getScaledWidth()/2 - 3+31, yHeight + mc.fontRendererObj.FONT_HEIGHT*2 - 2,
                    2, CyvForge.theme.highlight);

            if (!isSelected) {
                this.yawField.setFocused(false);
                this.pitchField.setFocused(false);
            }

            this.yawField.drawTextBox();
            this.pitchField.drawTextBox();

        }

        public boolean isPressed(int slotIndex, int mouseX, int mouseY, int mouseEvent) {
            float yHeight = (slotIndex + 1) * mc.fontRendererObj.FONT_HEIGHT*2 - scroll + (sr.getScaledHeight()/2 - sizeY/2);
            if (mouseX > xStart && mouseX < xStart + width && mouseY > yHeight && mouseY < yHeight + height) {
                return true;
            }

            return false;
        }

        public void mouseClicked(int slotIndex, int mouseX, int mouseY, int mouseEvent) {
            float yHeight = (slotIndex + 1) * mc.fontRendererObj.FONT_HEIGHT*2 - scroll + (sr.getScaledHeight()/2 - sizeY/2);
            if (!(mouseX > xStart && mouseX < xStart + width && mouseY > yHeight && mouseY < yHeight + height)) {
                return;
            }

            this.yawField.mouseClicked(mouseX, mouseY, mouseEvent);
            this.pitchField.mouseClicked(mouseX, mouseY, mouseEvent);

        }

    }

}
