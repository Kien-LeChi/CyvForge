package net.cyvforge.hud.nonlabels;

import net.cyvforge.hud.structure.DraggableHUDElement;
import net.cyvforge.hud.structure.ScreenPosition;
import net.cyvforge.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class DirectionHUD extends DraggableHUDElement {
    public DirectionHUD() {
        this.isDraggable = false;
    }

    @Override
    public ScreenPosition getDefaultPosition() {
        try {
            ScaledResolution window = new ScaledResolution(Minecraft.getMinecraft());
            return new ScreenPosition(window.getScaledWidth() / 2 - this.getWidth() / 2, 0);
        } catch (Exception e) {
            return new ScreenPosition(0, 0);
        }
    }

    @Override
    public ScreenPosition load() {
        return getDefaultPosition();
    }

    @Override
    public void save(ScreenPosition pos) {
    }

    @Override
    public String getName() {
        return "directionHUD";
    }

    @Override
    public String getDisplayName() {
        return "Direction HUD";
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void render(ScreenPosition pos) {
        this.position = this.getDefaultPosition();
        long color2 = 0xFFFFFFFF;
        float f = mc.getRenderViewEntity().rotationYaw;
        f = f % 360;
        if (f < 0) f += 360;

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        for (float i=0; i<360; i += 22.5) { //compass
            float distance = (Math.abs(f - i) <= 180) ? i-f : (f > 180) ? (i - (f - 360)) : ((i - 360) - f);
            if (Math.abs(distance) > 95) continue; //distance ranges from -90 to 90
            int xOffset = (int) ( distance * 0.5 * this.getWidth() / 100 );
            int height = (i%90==0) ? font.FONT_HEIGHT*2/3 : (i%45==0) ? font.FONT_HEIGHT/2 : font.FONT_HEIGHT/3;
            GuiUtils.drawVerticalLine(this.position.getAbsoluteX() + this.getWidth()/2 + xOffset,
                    this.position.getAbsoluteY()+1, this.position.getAbsoluteY()+1+height, 3, 0xFFFFFFFF, true);

            if (i==0) {//south
                drawString("S", this.position.getAbsoluteX() + this.getWidth()/2 + xOffset - font.getStringWidth("S")/2,
                        this.position.getAbsoluteY()+2+height, color2);
            } else if (i==90) {//west
                drawString("W", this.position.getAbsoluteX() + this.getWidth()/2 + xOffset - font.getStringWidth("W")/2,
                        this.position.getAbsoluteY()+2+height, color2);
            } else if (i==180) {//north
                drawString("N", this.position.getAbsoluteX() + this.getWidth()/2 + xOffset - font.getStringWidth("N")/2,
                        this.position.getAbsoluteY()+2+height, color2);
            } else if (i==270) {//east
                drawString("E", this.position.getAbsoluteX() + this.getWidth()/2 + xOffset - font.getStringWidth("E")/2,
                        this.position.getAbsoluteY()+2+height, color2);
            }

        }

        GuiUtils.drawVerticalLine(this.position.getAbsoluteX() + this.getWidth()/2, this.position.getAbsoluteY()+1,
                this.position.getAbsoluteY() + font.FONT_HEIGHT*3/2, 3, 0xFFFF0000, true);

    }

    @Override
    public void renderDummy(ScreenPosition pos) {
        this.render(pos);

    }
}