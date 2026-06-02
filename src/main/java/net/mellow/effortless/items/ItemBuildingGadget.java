package net.mellow.effortless.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.buildmode.BaseBuildMode;
import net.mellow.effortless.buildmode.BuildModes;
import net.mellow.effortless.buildmode.modes.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class ItemBuildingGadget extends Item implements IItemRenderPreview {

    public static enum BuildingMode {
        // EXTENDED(new Extended()), // greater reach
        LINE(new Line()); // lines
        // WALL(new Wall()), // walls
        // FLOOR(new Floor()); // floors

        public BaseBuildMode handler;

        private BuildingMode(BaseBuildMode handler) {
            this.handler = handler;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool) {
        BlockMeta selected = getSelected(stack);
        BuildingMode mode = getMode(stack);

        list.add("mode:  " + mode);

        list.add("block: " + selected.block.getUnlocalizedName());
        list.add("meta:  " + selected.meta);
    }

    // IF WE PUT ROCKS IN THE SHAPE OF A RUNWAY GOD WILL GIVE US HIGH-FRUCTOSE CORN SYRUP
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();

        MovingObjectPosition mop = BuildModes.getMop(player, 32);

        if (player.isSneaking()) {
            if (!world.isRemote) {
                if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
                    // select the hovered block, maybe also temporary, we'll see

                    int x = mop.blockX;
                    int y = mop.blockY;
                    int z = mop.blockZ;
    
                    BlockMeta target = new BlockMeta(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
                    setSelected(stack, target);
                } else {
                    // temporary shitty hack to skip implementing a GUI just yet

                    int mode = getMode(stack).ordinal();
                    mode += 1;
                    if (mode >= BuildingMode.values().length) mode = 0;

                    getMode(stack).handler.clear(stack);
                    setMode(stack, BuildingMode.values()[mode]);
                }
            }
        } else {
            BuildingMode mode = getMode(stack);
            mode.handler.add(stack, getSelected(stack), world, player, mop);
        }

        return stack;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        getMode(stack).handler.clear(stack);
        return false;
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


    // mode is stored as a string so inserting new modes won't fuck up existing tools
    public static BuildingMode getMode(ItemStack stack) {
        if (stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("mode")) return BuildingMode.LINE;
        try {
            return BuildingMode.valueOf(stack.stackTagCompound.getString("mode"));
        } catch (IllegalArgumentException ex) {
            return BuildingMode.LINE;
        }
    }

    public static void setMode(ItemStack stack, BuildingMode mode) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
        stack.stackTagCompound.setString("mode", mode.name());
    }


    @Override
    public void render(World world, EntityPlayer player, ItemStack stack, float partialTicks) {
        getMode(stack).handler.render(stack, world, player, partialTicks);
    }

}
