package net.mellow.effortless.buildmode.modes;

import java.util.ArrayList;
import java.util.List;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.blocks.Vec3;
import net.mellow.effortless.buildmode.BuildModes;
import net.mellow.effortless.buildmode.TwoClicksBuildMode;
import net.mellow.effortless.buildmode.ModeOptions.BuildingAction;
import net.mellow.effortless.buildmode.ModeOptions.BuildingOption;
import net.mellow.effortless.items.ItemBuildingGadget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Floor extends TwoClicksBuildMode {

    @Override
    public int add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, BlockPos from, int placedMeta) {
        BlockPos to = findFloor(player, from, true);
        if (to == null) return 0;
        
        BuildingAction fillMode = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);

        if (fillMode == BuildingAction.HOLLOW) {
            if (from.x != to.x && from.z != to.z) {
                return buildHollowFloor(world, player, selected, placedMeta, from, to, false);
            }
        }
        
        return buildBox(world, player, selected, placedMeta, from, to, false);
    }

    @Override
    public void clear(ItemStack stack) {
        stack.stackTagCompound.removeTag("pos0");
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, BlockPos from, float partialTicks) {
        BlockPos to = findFloor(player, from, true);
        if (to == null) return;
        
        renderBox(player, partialTicks, from, to, true);
        
        BuildingAction fillMode = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);
        if (fillMode == BuildingAction.HOLLOW && (Math.abs(from.x - to.x) > 1 && Math.abs(from.z - to.z) > 1)) {
            BlockPos min = BlockPos.min(from, to);
            BlockPos max = BlockPos.max(from, to);
            
            renderBox(player, partialTicks, min.add(1, 0, 1), max.add(-1, 0, -1));
        }
    }

    
    public static BlockPos findFloor(EntityPlayer player, BlockPos firstPos, boolean skipRaytrace) {
        Vec3 look = BuildModes.getPlayerLookVec(player);
        Vec3 start = BuildModes.getPlayerPos(player);

        List<Criteria> criteriaList = new ArrayList<>(3);

        //Y
        Vec3 yBound = BuildModes.findYBound(firstPos.y, start, look);
        criteriaList.add(new Criteria(yBound, start));

        //Remove invalid criteria
        // int reach = CapabilityHandler.getBuildModeReach(player);
        int reach = 32;
        criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

        //If none are valid, return empty list of blocks
        if (criteriaList.isEmpty()) return null;

        //Then only 1 can be valid, return that one
        Criteria selected = criteriaList.get(0);

        return BlockPos.containing(selected.planeBound);
    }

    static class Criteria {
        Vec3 planeBound;
        double distToPlayerSq;

        Criteria(Vec3 planeBound, Vec3 start) {
            this.planeBound = planeBound;
            this.distToPlayerSq = this.planeBound.distanceToSqr(start);
        }

        //check if its not behind the player and its not too close and not too far
        //also check if raytrace from player to block does not intersect blocks
        public boolean isValid(Vec3 start, Vec3 look, int reach, EntityPlayer player, boolean skipRaytrace) {
            return BuildModes.isCriteriaValid(start, look, reach, player, skipRaytrace, planeBound, planeBound, distToPlayerSq);
        }
    }
}
