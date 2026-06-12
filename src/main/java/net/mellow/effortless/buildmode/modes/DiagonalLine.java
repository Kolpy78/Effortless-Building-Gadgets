package net.mellow.effortless.buildmode.modes;

import java.util.ArrayList;
import java.util.List;

import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.blocks.PlaceableStack;
import net.mellow.effortless.blocks.Vec3;
import net.mellow.effortless.buildmode.ThreeClicksBuildMode;
import net.mellow.effortless.buildmode.VoxelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DiagonalLine extends ThreeClicksBuildMode {

    @Override
    public BlockPos addMid(ItemStack stack, World world, EntityPlayer player, BlockPos pos0) {
        return Floor.findFloor(player, pos0, true);
    }

    @Override
    public int add(ItemStack stack, PlaceableStack selected, World world, EntityPlayer player, BlockPos pos0, BlockPos pos1) {
        BlockPos pos2 = Cube.findHeight(player, pos1, true);
        if (pos2 == null) return 0;

        return build(world, player, selected, getDiagonalLineBlocks(pos0, pos2, 10), false);
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, BlockPos pos0, float partialTicks) {
        BlockPos pos1 = Floor.findFloor(player, pos0, true);
        if (pos1 == null) return;

        VoxelRenderer.renderBlocks(getDiagonalLineBlocks(pos0, pos1, 10), player, partialTicks);

        updateHighlight(pos0, pos1);
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, BlockPos pos0, BlockPos pos1, float partialTicks) {
        BlockPos pos2 = Cube.findHeight(player, pos1, true);
        if (pos2 == null) return;

        VoxelRenderer.renderBlocks(getDiagonalLineBlocks(pos0, pos2, 10), player, partialTicks);

        updateHighlight(pos0, pos2);
    }

    //Add diagonal line from first to second
    public static List<BlockPos> getDiagonalLineBlocks(BlockPos from, BlockPos to, float sampleMultiplier) {
        List<BlockPos> list = new ArrayList<>();

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
