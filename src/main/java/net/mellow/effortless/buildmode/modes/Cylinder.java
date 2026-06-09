package net.mellow.effortless.buildmode.modes;

import java.util.ArrayList;
import java.util.List;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.buildmode.ModeOptions.BuildingAction;
import net.mellow.effortless.buildmode.ModeOptions.BuildingOption;
import net.mellow.effortless.buildmode.ThreeClicksBuildMode;
import net.mellow.effortless.items.ItemBuildingGadget;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Cylinder extends ThreeClicksBuildMode {

    @Override
    public BlockPos addMid(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, BlockPos pos0) {
        return Floor.findFloor(player, pos0, true);
    }

    @Override
    public int add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, BlockPos pos0, BlockPos pos1, int placedMeta) {
        BlockPos pos2 = Cube.findHeight(player, pos1, true);
        if (pos2 == null) return 0;

        BuildingAction start = ItemBuildingGadget.getAction(stack, BuildingOption.CIRCLE_START);
        BuildingAction fill = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);

        return build(world, player, selected, placedMeta, getCylinderBlocks(pos0, pos1, pos2, start == BuildingAction.CIRCLE_START_CORNER, fill == BuildingAction.FULL), false);
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, BlockPos pos0, float partialTicks) {
        BlockPos pos1 = Floor.findFloor(player, pos0, true);
        if (pos1 == null) return;

        BuildingAction start = ItemBuildingGadget.getAction(stack, BuildingOption.CIRCLE_START);
        BuildingAction fill = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);
        
        if (start == BuildingAction.CIRCLE_START_CORNER) {
            updateHighlight(BlockPos.min(pos0, pos1), BlockPos.max(pos0, pos1));
        } else {
            Circle.updateHighlightCentered(BlockPos.min(pos0, pos1), BlockPos.max(pos0, pos1));
        }
        
        Tessellator tess = Tessellator.instance;
        startLineDraw(tess, player, partialTicks);

        for (BlockPos pos : Circle.getCircleBlocks(pos0, pos1, start == BuildingAction.CIRCLE_START_CORNER, fill == BuildingAction.FULL)) {
            drawFullBox(tess, pos, pos);
        }

        endLineDraw(tess);
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, BlockPos pos0, BlockPos pos1, float partialTicks) {
        BlockPos pos2 = Cube.findHeight(player, pos1, true);
        if (pos2 == null) return;

        BuildingAction start = ItemBuildingGadget.getAction(stack, BuildingOption.CIRCLE_START);
        BuildingAction fill = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);
        
        if (start == BuildingAction.CIRCLE_START_CORNER) {
            updateHighlight(BlockPos.min(pos0, pos2), BlockPos.max(pos0, pos2));
        } else {
            Circle.updateHighlightCentered(BlockPos.min(pos0, pos2), BlockPos.max(pos0, pos2));
        }

        Tessellator tess = Tessellator.instance;
        startLineDraw(tess, player, partialTicks);

        for (BlockPos pos : getCylinderBlocks(pos0, pos1, pos2, start == BuildingAction.CIRCLE_START_CORNER, fill == BuildingAction.FULL)) {
            drawFullBox(tess, pos, pos);
        }

        endLineDraw(tess);
    }

	public static List<BlockPos> getCylinderBlocks(BlockPos from, BlockPos mid, BlockPos to, boolean fromCorner, boolean fill) {
		List<BlockPos> list = new ArrayList<>();

		//Get circle blocks (using CIRCLE_START and FILL options built-in)
		List<BlockPos> circleBlocks = Circle.getCircleBlocks(from, mid, fromCorner, fill);

		int lowest = Math.min(from.y, to.y);
		int highest = Math.max(from.y, to.y);

		//Copy circle on y axis
		for (int y = lowest; y <= highest; y++) {
			for (BlockPos blockPos : circleBlocks) {
				list.add(new BlockPos(blockPos.x, y, blockPos.z));
			}
		}

		return list;
	}
    
}
