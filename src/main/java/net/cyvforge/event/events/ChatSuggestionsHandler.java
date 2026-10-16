package net.cyvforge.event.events;

import net.cyvforge.event.CommandInitializer;
import net.cyvforge.util.GuiUtils;
import net.cyvforge.util.defaults.CyvCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatSuggestionsHandler {

    private static class Suggestion {
        String text;
        boolean isHint;

        Suggestion(String text, boolean isHint) {
            this.text = text;
            this.isHint = isHint;
        }
    }

    private final List<String> baseCommands = Arrays.asList("cyv", "mm", "mpk");

    private static List<Suggestion> currentSuggestions = new ArrayList<>();
    private static int selectedIndex = -1;
    private static int scrollOffset = 0;
    private static String lastProcessedText = "";

    private static java.lang.reflect.Field inputFieldField;

    @SubscribeEvent
    public void onRenderChat(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CHAT) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) {
            currentSuggestions.clear();
            return;
        }

        try {
            if (inputFieldField == null) {
                inputFieldField = ReflectionHelper.findField(GuiChat.class, "inputField", "field_146415_a");
                inputFieldField.setAccessible(true);
            }
            GuiTextField inputField = (GuiTextField) inputFieldField.get(mc.currentScreen);
            String text = inputField.getText();

            if (!text.equals(lastProcessedText)) {
                updateSuggestions(text);
                lastProcessedText = text;
                selectedIndex = -1;
                scrollOffset = 0;
            }

            if (!currentSuggestions.isEmpty()) {
                renderSuggestions(currentSuggestions, inputField);
            }

        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) return;
        if (currentSuggestions.isEmpty()) return;

        int keyCode = Keyboard.getEventKey();
        if (!Keyboard.getEventKeyState()) return;

        if (keyCode == Keyboard.KEY_TAB) {
            boolean hasClickable = false;
            for (Suggestion s : currentSuggestions) if (!s.isHint) hasClickable = true;
            if (!hasClickable) return;

            int nextIndex = selectedIndex;
            int attempts = 0;

            do {
                if (keyCode == Keyboard.KEY_UP) {
                    nextIndex--;
                    if (nextIndex < 0) nextIndex = currentSuggestions.size() - 1;
                } else {
                    nextIndex++;
                    if (nextIndex >= currentSuggestions.size()) nextIndex = 0;
                }
                attempts++;
                if (attempts > currentSuggestions.size()) break;
            } while (currentSuggestions.get(nextIndex).isHint);

            if (!currentSuggestions.get(nextIndex).isHint) {
                selectedIndex = nextIndex;

                if (selectedIndex >= scrollOffset + 10) scrollOffset = selectedIndex - 9;
                else if (selectedIndex < scrollOffset) scrollOffset = selectedIndex;

                updateChatText(currentSuggestions.get(selectedIndex).text);
                event.setCanceled(true);
            }
        }
    }

    private void updateSuggestions(String text) {
        currentSuggestions.clear();
        if (!text.startsWith("/")) return;

        String[] parts = text.split(" ", -1);
        String firstWord = parts[0].substring(1).toLowerCase();

        if (!baseCommands.contains(firstWord)) return;

        if (parts.length == 2) {
            String subInput = parts[1].toLowerCase();
            for (CyvCommand cmd : CommandInitializer.cyvCommands) {
                if (subInput.isEmpty() || cmd.name.toLowerCase().startsWith(subInput)) {
                    currentSuggestions.add(new Suggestion(cmd.name, false));
                }
            }
        } else if (parts.length > 2) {
            String subCmdName = parts[1].toLowerCase();
            CyvCommand foundSub = null;
            for (CyvCommand c : CommandInitializer.cyvCommands) {
                if (c.name.equalsIgnoreCase(subCmdName) || (c.aliases != null && c.aliases.contains(subCmdName))) {
                    foundSub = c;
                    break;
                }
            }
            if (foundSub != null) {
                currentSuggestions = getArgSuggestions(foundSub, parts);
            }
        }
    }

    private List<Suggestion> getArgSuggestions(CyvCommand cmd, String[] parts) {
        List<Suggestion> args = new ArrayList<>();
        String currentTyping = parts[parts.length - 1].toLowerCase();

        String[] cmdArgs = new String[parts.length - 1];
        System.arraycopy(parts, 1, cmdArgs, 0, parts.length - 1);

        List<String> options = cmd.getTabCompletions(cmdArgs);

        if (options != null && !options.isEmpty()) {
            List<String> alreadyTyped = new ArrayList<>();
            for (int i = 1; i < cmdArgs.length - 1; i++) alreadyTyped.add(cmdArgs[i].toLowerCase());

            for (String opt : options) {
                if (!alreadyTyped.contains(opt.toLowerCase())) {
                    if (currentTyping.isEmpty() || opt.toLowerCase().startsWith(currentTyping)) {
                        args.add(new Suggestion(opt, false));
                    }
                }
            }
        }

        if (args.isEmpty() && cmd.usage != null && !cmd.usage.equalsIgnoreCase("none")) {
            args.add(new Suggestion(cmd.usage, true));
        }

        return args;
    }

    private void renderSuggestions(List<Suggestion> suggestions, GuiTextField inputField) {
        Minecraft mc = Minecraft.getMinecraft();

        int globalMaxWidth = 0;
        for (Suggestion s : suggestions) {
            int w = mc.fontRendererObj.getStringWidth(s.text);
            if (w > globalMaxWidth) globalMaxWidth = w;
        }

        int boxWidth = globalMaxWidth + 4;
        int visibleCount = Math.min(suggestions.size(), 10);
        int boxHeight = visibleCount * 12;

        String fullText = inputField.getText();
        int lastSpace = fullText.lastIndexOf(" ");
        String textBefore = (lastSpace != -1) ? fullText.substring(0, lastSpace + 1) : "";
        int xPos = inputField.xPosition + mc.fontRendererObj.getStringWidth(textBefore);
        int yPosBase = inputField.yPosition - boxHeight - 2;

        GuiUtils.drawRoundedRect(xPos, yPosBase, xPos + boxWidth, yPosBase + boxHeight, 0, 0xBF000000);

        for (int i = 0; i < visibleCount; i++) {
            int index = i + scrollOffset;
            Suggestion match = suggestions.get(index);

            int lineY = yPosBase + (i * 12);
            int textColor;

            if (match.isHint) {
                textColor = 0xFFAAAAAA;
            } else {
                textColor = (index == selectedIndex) ? 0xFFFFFF00 : 0xFFAAAAAA;
            }

            mc.fontRendererObj.drawStringWithShadow(match.text, xPos + 2, lineY + 2, textColor);
        }
    }

    private void updateChatText(String completion) {
        try {
            GuiChat gui = (GuiChat) Minecraft.getMinecraft().currentScreen;

            if (inputFieldField == null) {
                inputFieldField = ReflectionHelper.findField(GuiChat.class, "inputField", "field_146415_a");
                inputFieldField.setAccessible(true);
            }
            GuiTextField inputField = (GuiTextField) inputFieldField.get(gui);

            String currentText = inputField.getText();
            int lastSpace = currentText.lastIndexOf(" ");
            String newText = (lastSpace != -1 ? currentText.substring(0, lastSpace + 1) : "") + completion;
            inputField.setText(newText);
            lastProcessedText = newText;
        } catch (Exception ignored) {}
    }
}