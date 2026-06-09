package net.mellow.effortless.buildmode.modes;

import java.util.ArrayList;
import java.util.List;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.blocks.Vec3;
import net.mellow.effortless.buildmode.BaseBuildMode;
import net.mellow.effortless.buildmode.BuildModes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class DiagonalLine extends BaseBuildMode {

    @Override
    public int add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, MovingObjectPosition mop) {
        BlockPos pos0 = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos0"));
        BlockPos pos1 = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos1"));

        if (pos0 == null) {
            pos0 = BlockPos.fromRaycastSide(mop);
            if (pos0 == null) return 0;

            int placedMeta = getFinalPlacedMeta(selected, world, player, pos0.x, pos0.y, pos0.z, mop.sideHit, new Vec3(mop.hitVec));

            stack.stackTagCompound.setInteger("placedMeta", placedMeta);
            stack.stackTagCompound.setTag("pos0", pos0.save());
        } else if (pos1 == null) {
            pos1 = Floor.findFloor(player, pos0, true);
            if (pos1 == null) return 0;

            stack.stackTagCompound.setTag("pos1", pos1.save());
        } else {
            BlockPos pos2 = Cube.findHeight(player, pos1, true);
            if (pos2 == null) return 0;

            int placedMeta = stack.stackTagCompound.getInteger("placedMeta");

            clear(stack);

            return build(world, player, selected, placedMeta, getDiagonalLineBlocks(pos0, pos2), false);
        }
        
        return 0;
    }

    @Override
    public void clear(ItemStack stack) {
        stack.stackTagCompound.removeTag("pos0");
        stack.stackTagCompound.removeTag("pos1");
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, float partialTicks) {
        BlockPos pos0 = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos0"));
        BlockPos pos1 = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos1"));

        if (pos0 == null) {
            MovingObjectPosition mop = BuildModes.getMop(player, reach(stack));
            if (mop == null) return;

            Minecraft.getMinecraft().renderGlobal.drawSelectionBox(player, mop, 0, partialTicks);
        } else if (pos1 == null) {
            pos1 = Floor.findFloor(player, pos0, true);
            if (pos1 == null) return;

            Tessellator tess = Tessellator.instance;
            startLineDraw(tess, player, partialTicks);

            for (BlockPos pos : getDiagonalLineBlocks(pos0, pos1)) {
                drawFullBox(tess, pos, pos);
            }

            endLineDraw(tess);
        } else {
            BlockPos pos2 = Cube.findHeight(player, pos1, true);
            if (pos2 == null) return;

            Tessellator tess = Tessellator.instance;
            startLineDraw(tess, player, partialTicks);

            for (BlockPos pos : getDiagonalLineBlocks(pos0, pos2)) {
                drawFullBox(tess, pos, pos);
            }

            endLineDraw(tess);
        }
    }

    //Add diagonal line from first to second
    public static List<BlockPos> getDiagonalLineBlocks(BlockPos from, BlockPos to) {
		List<BlockPos> list = new ArrayList<>();
        float sampleMultiplier = 10;

		Vec3 first = Vec3.atCenterOf(from);
		Vec3 second = Vec3.atCenterOf(to);

		int iterations = (int) Math.ceil(first.distanceTo(second) * sampleMultiplier);
		for (double t = 0; t <= 1.0; t += 1.0 / iterations) {
			Vec3 lerp = first.add(second.subtract(first).scale(t));
			BlockPos candidate = BlockPos.containing(lerp);
			//Only add if not equal to the last in the list
			if (list.isEmpty() || !list.get(list.size() - 1).equals(candidate))
				list.add(candidate);
		}

		return list;
    }
    
}
