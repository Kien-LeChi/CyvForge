package net.cyvforge.command;

import net.cyvforge.CyvForge;
import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.event.ConfigLoader;
import net.cyvforge.event.events.MacroRecorder;
import net.cyvforge.recording.ClipViewer;
import net.cyvforge.recording.RollingClipBuffer;
import net.cyvforge.util.defaults.CyvCommand;
import net.minecraft.command.ICommandSender;

public class CommandClip extends CyvCommand {
    public CommandClip() {
        super("clip");
        this.helpString = "Capture recent ticks. /cyv clip seconds <1-600>, show, hide, tick <number>.";
    }

    @Override
    public void run(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ClipViewer.requestCapture();
        } else if (args[0].equalsIgnoreCase("show") && args.length == 1) {
            ClipViewer.show();
        } else if ((args[0].equalsIgnoreCase("hide") || args[0].equalsIgnoreCase("clear")) && args.length == 1) {
            ClipViewer.clear();
        } else if (args[0].equalsIgnoreCase("seconds")) {
            if (args.length == 1) {
                CyvForge.sendChatMessage("Clip history: " + CyvClientConfig.getInt("macroClipSeconds", 10) + " seconds.");
                return;
            }
            try {
                if (args.length != 2) throw new IllegalArgumentException();
                int seconds = Integer.parseInt(args[1]);
                if (seconds < 1 || seconds > RollingClipBuffer.MAX_SECONDS) throw new IllegalArgumentException();
                CyvClientConfig.set("macroClipSeconds", seconds);
                MacroRecorder.clipBuffer.resize(RollingClipBuffer.ticksForSeconds(seconds));
                ConfigLoader.save(CyvForge.config, true);
                CyvForge.sendChatMessage("Clip history set to " + seconds + " seconds (" + seconds * 20 + " ticks).");
            } catch (IllegalArgumentException e) {
                CyvForge.sendChatMessage("Usage: /cyv clip seconds <1-600>");
            }
        } else if (args[0].equalsIgnoreCase("tick") && args.length == 2) {
            try {
                int tick = Integer.parseInt(args[1]);
                if (tick < 1 || tick > ClipViewer.ticks().size()) throw new IllegalArgumentException();
                ClipViewer.select(tick - 1);
            } catch (IllegalArgumentException e) {
                CyvForge.sendChatMessage("Choose a tick from 1 to " + ClipViewer.ticks().size() + ".");
            }
        } else {
            CyvForge.sendChatMessage(helpString);
        }
    }
}
