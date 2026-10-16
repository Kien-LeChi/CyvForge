package net.cyvforge.command.config;

import net.cyvforge.CyvForge;
import net.cyvforge.config.CyvClientColorHelper;
import net.cyvforge.util.defaults.CyvCommand;
import net.minecraft.command.ICommandSender;
import java.util.List;

public class CommandColor extends CyvCommand {
    private final int number; //1 or 2

    public CommandColor(int number) {
        super("color" + number);
        this.number = number;
        this.hasArgs = true;
        this.usage = "[color]";
        this.helpString = "Sets color" + number + " for display and chat.";
    }

    @Override
    public void run(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            CyvForge.sendChatMessage("Invalid color. For a list of colors use /cyv colors");
            return;
        }

        boolean success = number == 1 ? CyvClientColorHelper.setColor1(args[0].toLowerCase()) : CyvClientColorHelper.setColor2(args[0].toLowerCase());

        if (success) {
            CyvForge.sendChatMessage("Successfully changed color " + number + ".");
        } else {
            CyvForge.sendChatMessage("Invalid color. For a list of colors use /cyv colors");
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return java.util.Arrays.asList(net.cyvforge.config.CyvClientColorHelper.colorStrings);
    }
}
