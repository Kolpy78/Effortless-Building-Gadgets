package net.mellow.effortless.buildmode;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.blocks.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public abstract class TwoClicksBuildMode extends BaseBuildMode {

    public abstract int add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, BlockPos from, int placedMeta);
    public abstract void render(ItemStack stack, World world, EntityPlayer player, BlockPos from, float partialTicks);

    @Override
    public int add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, MovingObjectPosition mop) {
        BlockPos from = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos0"));

        if (from == null) {
            from = BlockPos.fromRaycastSide(mop);
            if (from == null) return 0;

            int placedMeta = getFinalPlacedMeta(selected, world, player, from.x, from.y, from.z, mop.sideHit, new Vec3(mop.hitVec));

            stack.stackTagCompound.setTag("pos0", from.save());
            stack.stackTagCompound.setInteger("placedMeta", placedMeta);
        } else {
            int placedMeta = stack.stackTagCompound.getInteger("placedMeta");
            int built = add(stack, selected, world, player, from, placedMeta);

            if (built <= 0) return 0;

            clear(stack);
            
            return built;
        }

        return 0;
    }

    @Override
    public void clear(ItemStack stack) {
        stack.stackTagCompound.removeTag("pos0");
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, float partialTicks) {
        BlockPos from = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos0"));

        if (from == null) {
            MovingObjectPosition mop = BuildModes.getMop(player, reach(stack));
            if (mop != null) {
                Minecraft.getMinecraft().renderGlobal.drawSelectionBox(player, mop, 0, partialTicks);
            }
        } else {
            render(stack, world, player, from, partialTicks);
        }
    }
    
}
