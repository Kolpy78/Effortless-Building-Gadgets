package net.mellow.effortless.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class PlaceableStack {

    // Stores an item stack _and_ how it should be placed into the world
    
    public final ItemStack stack;
    public final BlockMeta place;

    public PlaceableStack(ItemStack stack, BlockMeta place) {
        this.stack = stack;
        this.place = place;
    }

    public PlaceableStack(ItemStack stack, Block block, int meta) {
        this(stack, new BlockMeta(block, meta));
    }

    public static PlaceableStack load(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("stack") || !tag.hasKey("block") || !tag.hasKey("meta")) return null;
        ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack"));
        BlockMeta place = new BlockMeta(tag.getInteger("block"), tag.getInteger("meta"));
        return new PlaceableStack(stack, place);
    }

    public NBTTagCompound save() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound stackTag = new NBTTagCompound();
        stack.writeToNBT(stackTag);
        tag.setTag("stack", stackTag);
        tag.setInteger("block", Block.getIdFromBlock(place.block));
        tag.setInteger("meta", place.meta);
        return tag;
    }

    private static final SoundType SILENT_BLOCK = new Block.SoundType("stone", -1.0F, 1.0F);

    // will place and immediately remove the block once it finds the final meta
    // x y z is the final block position, not the block it is placed on
    public static PlaceableStack getPlaceableStack(ItemStack selected, World world, EntityPlayer player, int x, int y, int z, int side, Vec3 hitVector) {
        float subX = (float)hitVector.x - (float)x;
        float subY = (float)hitVector.y - (float)y;
        float subZ = (float)hitVector.z - (float)z;

        ItemBlock placingItem = (ItemBlock) selected.getItem();

        BlockMeta was = new BlockMeta(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
        int wasSize = selected.stackSize;
        SoundType wasSound = placingItem.field_150939_a.stepSound;

        // Fake placement by real player to correctly fetch all block data
        placingItem.field_150939_a.stepSound = SILENT_BLOCK;
        placingItem.onItemUse(selected, player, world, x, y, z, side, subX, subY, subZ);

        // get meta (and TE for blocks with extended data... in a bit when I get to it)
        int meta = world.getBlockMetadata(x, y, z);

        // Reset back to whatever the state was before placement
        selected.stackSize = wasSize;
        placingItem.field_150939_a.stepSound = wasSound;
        world.setBlock(x, y, z, was.block, was.meta, 2);

        PlaceableStack stack = new PlaceableStack(selected, placingItem.field_150939_a, meta);

        return stack;
    }

    public static boolean stackMatches(ItemStack stack1, ItemStack stack2) {
        return stack1 != null && stack1.stackSize > 0 && stack2 != null && stack2.stackSize > 0
            && (stack1.getItem() == stack2.getItem()
            && (!stack2.getHasSubtypes() || stack2.getItemDamage() == stack1.getItemDamage())
            && ItemStack.areItemStackTagsEqual(stack2, stack1));
    }

}
