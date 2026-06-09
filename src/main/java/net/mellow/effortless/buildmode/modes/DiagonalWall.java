package net.mellow.effortless.buildmode.modes;

import java.util.ArrayList;
import java.util.List;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.blocks.Vec3;
import net.mellow.effortless.buildmode.BaseBuildMode;
import net.mellow.effortless.buildmode.BuildModes;
import net.mellow.effortless.buildmode.ModeOptions.BuildingAction;
import net.mellow.effortless.buildmode.ModeOptions.BuildingOption;
import net.mellow.effortless.items.ItemBuildingGadget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class DiagonalWall extends BaseBuildMode {

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

            BuildingAction fillMode = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);
            List<BlockPos> blocks = fillMode == BuildingAction.HOLLOW
                ? getHollowDiagonalWallBlocks(pos0, pos1, pos2)
                : getDiagonalWallBlocks(pos0, pos1, pos2);

            return build(world, player, selected, placedMeta, blocks, false);
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

            for (BlockPos pos : DiagonalLine.getDiagonalLineBlocks(pos0, pos1, 1)) {
                drawFullBox(tess, pos, pos);
            }

            endLineDraw(tess);
        } else {
            BlockPos pos2 = Cube.findHeight(player, pos1, true);
            if (pos2 == null) return;

            BuildingAction fillMode = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);
            List<BlockPos> blocks = fillMode == BuildingAction.HOLLOW
                ? getHollowDiagonalWallBlocks(pos0, pos1, pos2)
                : getDiagonalWallBlocks(pos0, pos1, pos2);

            Tessellator tess = Tessellator.instance;
            startLineDraw(tess, player, partialTicks);

            for (BlockPos pos : blocks) {
                drawFullBox(tess, pos, pos);
            }

            endLineDraw(tess);
        }
    }

    //Add diagonal wall from first to second
    public static List<BlockPos> getDiagonalWallBlocks(BlockPos from, BlockPos mid, BlockPos to) {
        List<BlockPos> list = new ArrayList<>();

        //Get diagonal line blocks
        List<BlockPos> diagonalLineBlocks = DiagonalLine.getDiagonalLineBlocks(from, mid, 1);

        int lowest = Math.min(from.y, to.y);
        int highest = Math.max(from.y, to.y);

        //Copy diagonal line on y axis
        for (int y = lowest; y <= highest; y++) {
            for (BlockPos blockPos : diagonalLineBlocks) {
                list.add(new BlockPos(blockPos.x, y, blockPos.z));
            }
        }

        return list;
    }

    public static List<BlockPos> getHollowDiagonalWallBlocks(BlockPos from, BlockPos mid, BlockPos to) {
        List<BlockPos> list = new ArrayList<>();

        //Get diagonal line blocks
        List<BlockPos> diagonalLineBlocks = DiagonalLine.getDiagonalLineBlocks(from, mid, 1);

        int lowest = Math.min(from.y, to.y);
        int highest = Math.max(from.y, to.y);

        // Place bottom and top
        for (BlockPos blockPos : diagonalLineBlocks) {
            list.add(new BlockPos(blockPos.x, lowest, blockPos.z));
            list.add(new BlockPos(blockPos.x, highest, blockPos.z));
        }

        // Place caps
        for (int y = lowest; y <= highest; y++) {
            list.add(new BlockPos(from.x, y, from.z));
            list.add(new BlockPos(to.x, y, to.z));
        }

        return list;
    }
    
}
