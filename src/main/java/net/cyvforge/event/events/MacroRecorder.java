package net.cyvforge.event.events;

import net.cyvforge.command.mpk.CommandMacro;
import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.recording.ClipViewer;
import net.cyvforge.recording.RollingClipBuffer;
import net.cyvforge.recording.TickSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MacroRecorder {
    public static final RollingClipBuffer clipBuffer = new RollingClipBuffer();
    private EntityPlayerSP lastPlayer;
    private World lastWorld;
    private int lastDimension;
    private boolean[] pendingKeys;
    private double vx, vy, vz;
    private float lastYaw, lastPitch, yawDelta, pitchDelta;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player != lastPlayer || mc.theWorld != lastWorld
                || (player != null && player.dimension != lastDimension)) {
            clipBuffer.clear();
            ClipViewer.clear();
            pendingKeys = null;
            lastPlayer = player;
            lastWorld = mc.theWorld;
            if (player != null) {
                lastDimension = player.dimension;
                lastYaw = player.rotationYaw;
                lastPitch = player.rotationPitch;
            }
        }
        if (player == null || mc.theWorld == null || mc.isGamePaused()) {
            pendingKeys = null;
            return;
        }

        if (event.phase == TickEvent.Phase.END && pendingKeys != null) {
            TickSnapshot tick = new TickSnapshot(pendingKeys, player.isSprinting(),
                    player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, vx, vy, vz);
            clipBuffer.add(tick, RollingClipBuffer.ticksForSeconds(
                    CyvClientConfig.getInt("macroClipSeconds", RollingClipBuffer.DEFAULT_SECONDS)));
            if (CommandMacro.isRecording) CommandMacro.macro.add(tick.toMacroRow(yawDelta, pitchDelta));
            pendingKeys = null;
        }
    }

    // Player START runs after keyboard polling and macro injection, before movement.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.START || event.player != mc.thePlayer || mc.isGamePaused()) return;
        EntityPlayerSP player = mc.thePlayer;
        GameSettings gs = mc.gameSettings;
        pendingKeys = new boolean[] {gs.keyBindForward.isKeyDown(), gs.keyBindLeft.isKeyDown(),
                gs.keyBindBack.isKeyDown(), gs.keyBindRight.isKeyDown(), gs.keyBindJump.isKeyDown(),
                gs.keyBindSprint.isKeyDown(), gs.keyBindSneak.isKeyDown(), gs.keyBindUseItem.isKeyDown()};
        // Capture incoming motion BEFORE EntityLivingBase applies its 0.005 cutoff.
        vx = player.motionX;
        vy = player.motionY;
        vz = player.motionZ;
        yawDelta = player.rotationYaw - lastYaw;
        pitchDelta = player.rotationPitch - lastPitch;
        lastYaw = player.rotationYaw;
        lastPitch = player.rotationPitch;
    }
}
