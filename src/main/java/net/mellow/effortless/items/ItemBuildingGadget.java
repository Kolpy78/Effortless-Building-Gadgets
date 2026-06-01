package net.mellow.effortless.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemBuildingGadget extends Item {

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

}
