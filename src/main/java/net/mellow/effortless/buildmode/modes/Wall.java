package net.mellow.effortless.buildmode.modes;

import java.util.ArrayList;
import java.util.List;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.buildmode.BaseBuildMode;
import net.mellow.effortless.buildmode.BuildModes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Wall extends BaseBuildMode {

    @Override
    public void add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, MovingObjectPosition mop) {
        BlockPos from = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos0"));

        if (from == null) {
            from = BlockPos.fromRaycastSide(mop);
            if (from == null) return;

            stack.stackTagCompound.setTag("pos0", from.save());
        } else {
            BlockPos to = findWall(player, from, true);

            buildBox(world, player, selected, from, to, false);

            clear(stack);
        }
    }

    @Override
    public void clear(ItemStack stack) {
        stack.stackTagCompound.removeTag("pos0");
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, float partialTicks) {
        BlockPos from = BlockPos.load(stack.stackTagCompound.getCompoundTag("pos0"));
        if (from == null) {
            BlockPos pos = BlockPos.fromRaycastSide(BuildModes.getMop(player, 32));
            if (pos != null) {
                renderBox(player, partialTicks, pos, pos);
            }
            return;
        }

        BlockPos to = findWall(player, from, true);;
        if (to == null) return;

        renderBox(player, partialTicks, from, to);
    }
    

    public static BlockPos findWall(EntityPlayer player, BlockPos firstPos, boolean skipRaytrace) {
        Vec3 look = BuildModes.getPlayerLookVec(player);
        Vec3 start = BuildModes.getPlayerPos(player);

        List<Criteria> criteriaList = new ArrayList<>(3);

        //X
        Vec3 xBound = BuildModes.findXBound(firstPos.x, start, look);
        criteriaList.add(new Criteria(xBound, firstPos, start, look));

        //Z
        Vec3 zBound = BuildModes.findZBound(firstPos.z, start, look);
        criteriaList.add(new Criteria(zBound, firstPos, start, look));

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
            //Select the one that is closest
            //Limit the angle to not be too extreme
            for (int i = 1; i < criteriaList.size(); i++) {
                Criteria criteria = criteriaList.get(i);
                if (criteria.distToPlayerSq < selected.distToPlayerSq && Math.abs(criteria.angle) - Math.abs(selected.angle) < 3)
                    selected = criteria;
            }
        }

        return BlockPos.containing(selected.planeBound);
    }

    static class Criteria {
        Vec3 planeBound;
        double distToPlayerSq;
        double angle;

        Criteria(Vec3 planeBound, BlockPos firstPos, Vec3 start, Vec3 look) {
            this.planeBound = planeBound;
            this.distToPlayerSq = this.planeBound.squareDistanceTo(start);
            Vec3 wall = this.planeBound.subtract(Vec3.createVectorHelper(firstPos.x, firstPos.y, firstPos.z));
            this.angle = wall.xCoord * look.xCoord + wall.zCoord * look.zCoord; //dot product ignoring y (looking up/down should not affect this angle)
        }

        //check if its not behind the player and its not too close and not too far
        //also check if raytrace from player to block does not intersect blocks
        public boolean isValid(Vec3 start, Vec3 look, int reach, EntityPlayer player, boolean skipRaytrace) {
            return BuildModes.isCriteriaValid(start, look, reach, player, skipRaytrace, planeBound, planeBound, distToPlayerSq);
        }
    }
    
}
