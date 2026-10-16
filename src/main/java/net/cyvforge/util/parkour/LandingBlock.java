package net.cyvforge.util.parkour;

import net.cyvforge.CyvForge;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LandingBlock {

    public BlockPos pos;
    public AxisAlignedBB[] bb; //bounding boxes
    public LandingMode mode;
    public LandingAxis axis;
    public boolean isBox;
    public boolean neoAndNormal;
    public boolean isLiquid = false;
    public double liquidBottomOffset = 0;
    public int targetTick = -1;

    public Double pb;
    public Double pbX;
    public Double pbZ;

    public Double lastOffsetX;
    public Double lastOffsetZ;
    public Double lastPb;

    public Double xMaxWall, xMinWall, zMaxWall, zMinWall;

    public double xMinCond, xMaxCond, zMinCond, zMaxCond; //detection boxes (for offsets)

    public LandingBlock(BlockPos pos, LandingMode mode, LandingAxis axis, boolean isBox) {
        this.pos = pos;
        this.mode = mode;
        this.axis = axis;
        this.isBox = isBox;

        calculateBounds(); //calculate the hitbox
        calculateWalls();

        //create detection box
        this.xMinCond = this.smallestX() - 1;
        this.xMaxCond = this.largestX() + 1;
        this.zMinCond = this.smallestZ() - 1;
        this.zMaxCond = this.largestZ() + 1;
    }

    public LandingBlock(AxisAlignedBB bounds) {
        this.pos = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
        this.mode = LandingMode.landing;
        this.axis = LandingAxis.both;
        this.isBox = false;

        this.bb = new AxisAlignedBB[] {bounds};

        this.xMinCond = this.smallestX() - 1;
        this.xMaxCond = this.largestX() + 1;
        this.zMinCond = this.smallestZ() - 1;
        this.zMaxCond = this.largestZ() + 1;
    }

    private void calculateBounds() {
        if (this.pos == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;

        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (block instanceof net.minecraft.block.BlockLiquid) {
            this.isLiquid = true;
            double shrink = 0.0;
            double bottomOffset = 0.0;

            if (block.getMaterial() == net.minecraft.block.material.Material.water) { //water
                shrink = 0.001;
                bottomOffset = 0.399;
            }
            else if (block.getMaterial() == net.minecraft.block.material.Material.lava) { //lava
                shrink = 0.1;
                bottomOffset = 0.4;
            }

            this.liquidBottomOffset = bottomOffset;

            this.bb = new AxisAlignedBB[] {
                    new AxisAlignedBB(
                            pos.getX() + shrink, pos.getY() - bottomOffset, pos.getZ() + shrink,
                            pos.getX() + 1 - shrink, pos.getY() + 1, pos.getZ() + 1 - shrink
                    )
            };
            return;
        }

        //THIS IS TEMPORARY. I will find a better solution in the future
        if (isBox && (block instanceof BlockLadder || block instanceof BlockVine || block instanceof BlockSlime || block instanceof BlockIce ||
                block instanceof net.minecraft.block.BlockPackedIce)) {
            AxisAlignedBB playerHitbox = mc.thePlayer.getEntityBoundingBox();

            double playerX = playerHitbox.maxX - playerHitbox.minX;
            double playerZ = playerHitbox.maxZ - playerHitbox.minZ;
            AxisAlignedBB box = new AxisAlignedBB(playerX/2 + pos.getX(), pos.getY() - 0.02, playerZ/2 + pos.getZ(),
                    1-playerX/2 + pos.getX(), 1 + pos.getY(), 1-playerZ/2 + pos.getZ());
            this.bb = new AxisAlignedBB[] {box};

            return;
        }

        this.bb = CyvForge.getHitbox(pos, world).toArray(new AxisAlignedBB[0]);

    }

    public double smallestX() {
        Double v = null;
        for (AxisAlignedBB bb : this.bb) {
            if (v == null || v > bb.minX) v = bb.minX;
        }
        return v;
    }

    public double smallestY() {
        Double v = null;
        for (AxisAlignedBB bb : this.bb) {
            if (v == null || v > bb.minY) v = bb.minY;
        }
        return v;
    }

    public double smallestZ() {
        Double v = null;
        for (AxisAlignedBB bb : this.bb) {
            if (v == null || v > bb.minZ) v = bb.minZ;
        }
        return v;
    }

    public double largestX() {
        Double v = null;
        for (AxisAlignedBB bb : this.bb) {
            if (v == null || v < bb.maxX) v = bb.maxX;
        }
        return v;
    }

    public double largestY() {
        Double v = null;
        for (AxisAlignedBB bb : this.bb) {
            if (v == null || v < bb.maxY) v = bb.maxY;
        }
        return v;
    }

    public double largestZ() {
        Double v = null;
        for (AxisAlignedBB bb : this.bb) {
            if (v == null || v < bb.maxZ) v = bb.maxZ;
        }
        return v;
    }

    public void adjustCond(double x1, double x2, double z1, double z2) {
        if (x1 > x2) {
            this.xMaxCond = x1;
            this.xMinCond = x2;

        } else {
            this.xMaxCond = x2;
            this.xMinCond = x1;

        }

        if (z1 > z2) {
            this.zMaxCond = z1;
            this.zMinCond = z2;
        } else {
            this.zMaxCond = z2;
            this.zMinCond = z1;
        }

    }

    public void calculateWalls() {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;
        AxisAlignedBB playerHitbox = Minecraft.getMinecraft().thePlayer.getEntityBoundingBox();
        BlockPos tempPos = pos;
        if (this.mode == LandingMode.enter) tempPos = tempPos.down();

        xMinWall = null; xMaxWall = null; zMinWall = null; zMaxWall = null;

        double playerHeight = playerHitbox.maxY - playerHitbox.minY;
        double playerWidthX = playerHitbox.maxX - playerHitbox.minX;
        double playerWidthZ = playerHitbox.maxZ - playerHitbox.minZ;

        for (AxisAlignedBB box : bb) {
            ArrayList<AxisAlignedBB> wallBoxes = new ArrayList<AxisAlignedBB>();
            BlockPos currentWallPos = null;
            double offset, currentWall;

            // Z BACK (North: Z-)
            wallBoxes.clear();
            currentWallPos = tempPos.north();
            for (int dy = 0; dy <= 2; dy++) {
                List<AxisAlignedBB> h = CyvForge.getHitbox(currentWallPos.up(dy), world);
                if (h != null) wallBoxes.addAll(h);
            }

            for (int dy = 1; dy <= 2; dy++) {
                List<AxisAlignedBB> inBlock = CyvForge.getHitbox(tempPos.up(dy), world);
                if (inBlock != null) {
                    for (AxisAlignedBB bIn : inBlock) {
                        if (bIn.minZ <= box.minZ + 0.01 && bIn.maxZ < box.maxZ - 0.5) wallBoxes.add(bIn);
                    }
                }
            }

            for (AxisAlignedBB wall : wallBoxes) {
                if ((wall.maxX - wall.minX) < (box.maxX - box.minX) - 0.01) continue;
                if (wall.minX < box.minX - 0.1 && wall.maxX > box.maxX + 0.1 && (wall.maxZ - wall.minZ) < 0.5) continue;
                if (wall.maxY <= box.maxY) continue;
                if (wall.minY >= box.maxY + playerHeight) continue;

                currentWall = wall.maxZ;
                offset = box.minZ - currentWall - playerWidthZ;

                if (offset < 0) {
                    if (zMinWall == null || currentWall > zMinWall) zMinWall = currentWall;
                }
            }

            // Z FRONT (South: Z+)
            wallBoxes.clear();
            currentWallPos = tempPos.south();
            for (int dy = 0; dy <= 2; dy++) {
                List<AxisAlignedBB> h = CyvForge.getHitbox(currentWallPos.up(dy), world);
                if (h != null) wallBoxes.addAll(h);
            }

            for (int dy = 1; dy <= 2; dy++) {
                List<AxisAlignedBB> inBlock = CyvForge.getHitbox(tempPos.up(dy), world);
                if (inBlock != null) {
                    for (AxisAlignedBB bIn : inBlock) {
                        if (bIn.maxZ >= box.maxZ - 0.01 && bIn.minZ > box.minZ + 0.5) wallBoxes.add(bIn);
                    }
                }
            }

            for (AxisAlignedBB wall : wallBoxes) {
                if ((wall.maxX - wall.minX) < (box.maxX - box.minX) - 0.01) continue;
                if (wall.minX < box.minX - 0.1 && wall.maxX > box.maxX + 0.1 && (wall.maxZ - wall.minZ) < 0.5) continue;
                if (wall.maxY <= box.maxY) continue;
                if (wall.minY >= box.maxY + playerHeight) continue;

                currentWall = wall.minZ;
                offset = currentWall - box.maxZ - playerWidthZ;

                if (offset < 0) {
                    if (zMaxWall == null || currentWall < zMaxWall) zMaxWall = currentWall;
                }
            }

            // X RIGHT (West: X-)
            wallBoxes.clear();
            currentWallPos = tempPos.west();
            for (int dy = 0; dy <= 2; dy++) {
                List<AxisAlignedBB> h = CyvForge.getHitbox(currentWallPos.up(dy), world);
                if (h != null) wallBoxes.addAll(h);
            }

            for (int dy = 1; dy <= 2; dy++) {
                List<AxisAlignedBB> inBlock = CyvForge.getHitbox(tempPos.up(dy), world);
                if (inBlock != null) {
                    for (AxisAlignedBB bIn : inBlock) {
                        if (bIn.minX <= box.minX + 0.01 && bIn.maxX < box.maxX - 0.5) wallBoxes.add(bIn);
                    }
                }
            }

            for (AxisAlignedBB wall : wallBoxes) {
                if ((wall.maxZ - wall.minZ) < (box.maxZ - box.minZ) - 0.01) continue;
                if (wall.minZ < box.minZ - 0.1 && wall.maxZ > box.maxZ + 0.1 && (wall.maxX - wall.minX) < 0.5) continue;
                if (wall.maxY <= box.maxY) continue;
                if (wall.minY >= box.maxY + playerHeight) continue;

                currentWall = wall.maxX;
                offset = box.minX - currentWall - playerWidthX;

                if (offset < 0) {
                    if (xMinWall == null || currentWall > xMinWall) xMinWall = currentWall;
                }
            }

            // X LEFT (East: X+)
            wallBoxes.clear();
            currentWallPos = tempPos.east();
            for (int dy = 0; dy <= 2; dy++) {
                List<AxisAlignedBB> h = CyvForge.getHitbox(currentWallPos.up(dy), world);
                if (h != null) wallBoxes.addAll(h);
            }

            for (int dy = 1; dy <= 2; dy++) {
                List<AxisAlignedBB> inBlock = CyvForge.getHitbox(tempPos.up(dy), world);
                if (inBlock != null) {
                    for (AxisAlignedBB bIn : inBlock) {
                        if (bIn.maxX >= box.maxX - 0.01 && bIn.minX > box.minX + 0.5) wallBoxes.add(bIn);
                    }
                }
            }

            for (AxisAlignedBB wall : wallBoxes) {
                if ((wall.maxZ - wall.minZ) < (box.maxZ - box.minZ) - 0.01) continue;
                if (wall.minZ < box.minZ - 0.1 && wall.maxZ > box.maxZ + 0.1 && (wall.maxX - wall.minX) < 0.5) continue;
                if (wall.maxY <= box.maxY) continue;
                if (wall.minY >= box.maxY + playerHeight) continue;

                currentWall = wall.minX;
                offset = currentWall - box.maxX - playerWidthX;

                if (offset < 0) {
                    if (xMaxWall == null || currentWall < xMaxWall) xMaxWall = currentWall;
                }
            }
        }
    }

    public void resetWalls() {
        xMinWall = null; xMaxWall = null; zMinWall = null; zMaxWall = null;
    }
}
