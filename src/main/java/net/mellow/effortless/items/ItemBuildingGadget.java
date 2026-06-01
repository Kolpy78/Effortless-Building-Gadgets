package net.mellow.effortless.items;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.buildmode.BuildModes;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemBuildingGadget extends Item implements IItemRenderPreview {

    public static enum BuildingMode {
        EXTENDED, // greater reach
        LINE, // lines
        WALL, // walls
        FLOOR, // floors
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool) {
        BlockMeta selected = getSelected(stack);
        BuildingMode mode = getMode(stack);
        BlockPos from = getFromPosition(stack);

        list.add("mode:  " + mode);

        list.add("block: " + selected.block.getUnlocalizedName());
        list.add("meta:  " + selected.meta);

        if (from != null) list.add("from:  " + from);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                BlockMeta target = new BlockMeta(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
                setSelected(stack, target);
            }

            return true;
        } else {
            BlockPos from = getFromPosition(stack);
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            BlockPos pos = new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

            if (from == null) {
                setFromPosition(stack, pos);
            } else {
                buildLine(world, stack, getSelected(stack), from, pos);
            }
        }

        return false;
    }

    public static void buildLine(World world, ItemStack stack, BlockMeta selected, BlockPos from, BlockPos to) {
        int sx = Math.abs(from.x - to.x);
        int sy = Math.abs(from.y - to.y);
        int sz = Math.abs(from.z - to.z);

        int longest = Math.max(sx, Math.max(sy, sz));
        boolean lx = true, ly = true, lz = true;

        if (longest == sx) lx = false;
        else if (longest == sy) ly = false;
        else if (longest == sz) lz = false;

        build(world, selected, from, to, lx, ly, lz);

        clearFromPosition(stack);
    }

    public static void build(World world, BlockMeta selected, BlockPos from, BlockPos to, boolean lockX, boolean lockY, boolean lockZ) {
        if (lockX) to.x = from.x;
        if (lockY) to.y = from.y;
        if (lockZ) to.z = from.z;

        for (int x = Math.min(from.x, to.x); x <= Math.max(from.x, to.x); x++)
        for (int y = Math.min(from.y, to.y); y <= Math.max(from.y, to.y); y++)
        for (int z = Math.min(from.z, to.z); z <= Math.max(from.z, to.z); z++) {
            world.setBlock(x, y, z, selected.block, selected.meta, 3);
        }
    }

    public static BlockMeta getSelected(ItemStack stack) {
        if (stack.stackTagCompound == null) return new BlockMeta(Blocks.stone, 0);
        return new BlockMeta(stack.stackTagCompound.getInteger("block"), stack.stackTagCompound.getByte("meta"));
    }

    public static void setSelected(ItemStack stack, BlockMeta select) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
        stack.stackTagCompound.setInteger("block", Block.getIdFromBlock(select.block));
        stack.stackTagCompound.setByte("meta", (byte)select.meta);
    }

    public static BuildingMode getMode(ItemStack stack) {
        if (stack.stackTagCompound == null) return BuildingMode.LINE;
        return BuildingMode.LINE;
    }

    public static BlockPos getFromPosition(ItemStack stack) {
        if (stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("x") || !stack.stackTagCompound.hasKey("y") || !stack.stackTagCompound.hasKey("z"))
            return null;

        int x = stack.stackTagCompound.getInteger("x");
        int y = stack.stackTagCompound.getInteger("y");
        int z = stack.stackTagCompound.getInteger("z");

        return new BlockPos(x, y, z);
    }

    public static void setFromPosition(ItemStack stack, BlockPos pos) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();

        stack.stackTagCompound.setInteger("x", pos.x);
        stack.stackTagCompound.setInteger("y", pos.y);
        stack.stackTagCompound.setInteger("z", pos.z);
    }

    public static void clearFromPosition(ItemStack stack) {
        stack.stackTagCompound.removeTag("x");
        stack.stackTagCompound.removeTag("y");
        stack.stackTagCompound.removeTag("z");
    }

    @Override
    public void render(World world, EntityPlayer player, ItemStack stack, float partialTicks) {
        BlockPos from = getFromPosition(stack);

        if (from != null) {
            BlockPos to = findLine(player, from, true);

            if (to == null) return;

            // MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
            // ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
            // BlockPos to = new BlockPos(mop.blockX + dir.offsetX, mop.blockY + dir.offsetY, mop.blockZ + dir.offsetZ);
            
            double dx = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            double dy = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
            double dz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

            double minX = Math.min(from.x, to.x) + 0.125;
            double maxX = Math.max(from.x, to.x) + 0.875;
            double minY = Math.min(from.y, to.y) + 0.125;
            double maxY = Math.max(from.y, to.y) + 0.875;
            double minZ = Math.min(from.z, to.z) + 0.125;
            double maxZ = Math.max(from.z, to.z) + 0.875;
            
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f(1F, 1F, 1F);
            
            Tessellator tess = Tessellator.instance;
            tess.setTranslation(-dx, -dy, -dz);
            tess.startDrawing(GL11.GL_LINES);
            tess.setBrightness(240);
            tess.setColorRGBA_F(1F, 1F, 1F, 1F);
            
            // top
            tess.addVertex(minX, maxY, minZ);
            tess.addVertex(minX, maxY, maxZ);
            
            tess.addVertex(minX, maxY, maxZ);
            tess.addVertex(maxX, maxY, maxZ);
            
            tess.addVertex(maxX, maxY, maxZ);
            tess.addVertex(maxX, maxY, minZ);

            tess.addVertex(maxX, maxY, minZ);
            tess.addVertex(minX, maxY, minZ);
            
            // bottom
            tess.addVertex(minX, minY, minZ);
            tess.addVertex(minX, minY, maxZ);
            
            tess.addVertex(minX, minY, maxZ);
            tess.addVertex(maxX, minY, maxZ);
            
            tess.addVertex(maxX, minY, maxZ);
            tess.addVertex(maxX, minY, minZ);

            tess.addVertex(maxX, minY, minZ);
            tess.addVertex(minX, minY, minZ);

            // sides
            tess.addVertex(minX, minY, minZ);
            tess.addVertex(minX, maxY, minZ);

            tess.addVertex(maxX, minY, minZ);
            tess.addVertex(maxX, maxY, minZ);

            tess.addVertex(maxX, minY, maxZ);
            tess.addVertex(maxX, maxY, maxZ);

            tess.addVertex(minX, minY, maxZ);
            tess.addVertex(minX, maxY, maxZ);
            
            tess.draw();
            tess.setTranslation(0, 0, 0);
            
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    

    public static BlockPos findLine(EntityPlayer player, BlockPos firstPos, boolean skipRaytrace) {
        Vec3 look = BuildModes.getPlayerLookVec(player);
        Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        List<Criteria> criteriaList = new ArrayList<>(3);

        //X
        Vec3 xBound = BuildModes.findXBound(firstPos.x, start, look);
        criteriaList.add(new Criteria(xBound, firstPos, start));

        //Y
        Vec3 yBound = BuildModes.findYBound(firstPos.y, start, look);
        criteriaList.add(new Criteria(yBound, firstPos, start));

        //Z
        Vec3 zBound = BuildModes.findZBound(firstPos.z, start, look);
        criteriaList.add(new Criteria(zBound, firstPos, start));

        //Remove invalid criteria
        // int reach = CapabilityHandler.getBuildModeReach(player);
        int reach = 32;
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //If only 1 is valid, choose that one
        Criteria selected = criteriaList.get(0);

        //If multiple are valid, choose based on criteria
        if (criteriaList.size() > 1) {
            //Select the one that is closest (from wall position to its line counterpart)
            for (int i = 1; i < criteriaList.size(); i++) {
                Criteria criteria = criteriaList.get(i);
                if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
                    //Both very close to line, choose closest to player
                    if (criteria.distToPlayerSq < selected.distToPlayerSq)
                        selected = criteria;
                } else {
                    //Pick closest to line
                    if (criteria.distToLineSq < selected.distToLineSq)
                        selected = criteria;
                }
            }

        }

        return BlockPos.containing(selected.lineBound);
    }

    static class Criteria {
        Vec3 planeBound;
        Vec3 lineBound;
        double distToLineSq;
        double distToPlayerSq;

        Criteria(Vec3 planeBound, BlockPos firstPos, Vec3 start) {
            this.planeBound = planeBound;
            this.lineBound = toLongestLine(this.planeBound, firstPos);
            this.distToLineSq = this.lineBound.squareDistanceTo(this.planeBound);
            this.distToPlayerSq = this.planeBound.squareDistanceTo(start);
        }

        //Make it from a plane into a line
        //Select the axis that is longest
        private Vec3 toLongestLine(Vec3 boundVec, BlockPos firstPos) {
            BlockPos bound = BlockPos.containing(boundVec);

            BlockPos firstToSecond = bound.subtract(firstPos);
            firstToSecond = new BlockPos(Math.abs(firstToSecond.x), Math.abs(firstToSecond.y), Math.abs(firstToSecond.z));
            int longest = Math.max(firstToSecond.x, Math.max(firstToSecond.y, firstToSecond.z));
            if (longest == firstToSecond.x) {
                return Vec3.createVectorHelper(bound.x, firstPos.y, firstPos.z);
            }
            if (longest == firstToSecond.y) {
                return Vec3.createVectorHelper(firstPos.x, bound.y, firstPos.z);
            }
            if (longest == firstToSecond.z) {
                return Vec3.createVectorHelper(firstPos.x, firstPos.y, bound.z);
            }
            return null;
        }

        //check if its not behind the player and its not too close and not too far
        //also check if raytrace from player to block does not intersect blocks
        public boolean isValid(Vec3 start, Vec3 look, int reach, EntityPlayer player, boolean skipRaytrace) {
            return BuildModes.isCriteriaValid(start, look, reach, player, skipRaytrace, lineBound, planeBound, distToPlayerSq);
        }

    }

}
