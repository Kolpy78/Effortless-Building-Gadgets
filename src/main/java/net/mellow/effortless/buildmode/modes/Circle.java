package net.mellow.effortless.buildmode.modes;

import java.util.ArrayList;
import java.util.List;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.mellow.effortless.buildmode.ModeOptions.BuildingAction;
import net.mellow.effortless.buildmode.ModeOptions.BuildingOption;
import net.mellow.effortless.buildmode.TwoClicksBuildMode;
import net.mellow.effortless.items.ItemBuildingGadget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Circle extends TwoClicksBuildMode {

    @Override
    public int add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, BlockPos from, int placedMeta) {
        BlockPos to = Floor.findFloor(player, from, true);
        if (to == null) return 0;

        BuildingAction start = ItemBuildingGadget.getAction(stack, BuildingOption.CIRCLE_START);
        BuildingAction fill = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);

        return build(world, player, selected, placedMeta, getCircleBlocks(from, to, start == BuildingAction.CIRCLE_START_CORNER, fill == BuildingAction.FULL), false);
    }

    @Override
    public void render(ItemStack stack, World world, EntityPlayer player, BlockPos from, float partialTicks) {
        BlockPos to = Floor.findFloor(player, from, true);
        if (to == null) return;

        BuildingAction start = ItemBuildingGadget.getAction(stack, BuildingOption.CIRCLE_START);
        BuildingAction fill = ItemBuildingGadget.getAction(stack, BuildingOption.FILL);
        
        if (start == BuildingAction.CIRCLE_START_CORNER) {
            updateHighlight(BlockPos.min(from, to), BlockPos.max(from, to));
        } else {
            BlockPos min = BlockPos.min(from, to);
            BlockPos max = BlockPos.max(from, to);

            List<String> values = new ArrayList<>();
            if (min.x != max.x) values.add("" + ((max.x - min.x + 1) * 2 - 1));
            if (min.y != max.y) values.add("" + ((max.y - min.y + 1) * 2 - 1));
            if (min.z != max.z) values.add("" + ((max.z - min.z + 1) * 2 - 1));

            highlightTitle = !values.isEmpty() ? String.join("x", values) : "1";
            Minecraft.getMinecraft().ingameGUI.remainingHighlightTicks = 40;
        }
        
        Tessellator tess = Tessellator.instance;
        startLineDraw(tess, player, partialTicks);

        for (BlockPos pos : getCircleBlocks(from, to, start == BuildingAction.CIRCLE_START_CORNER, fill == BuildingAction.FULL)) {
            drawFullBox(tess, pos, pos);
        }

        endLineDraw(tess);
    }

    public static List<BlockPos> getCircleBlocks(BlockPos from, BlockPos to, boolean fromCorner, boolean fill) {
        List<BlockPos> list = new ArrayList<>();

        double centerX = from.x;
        double centerZ = from.z;

        //Adjust for CIRCLE_START
        if (fromCorner) {
            centerX = from.x + (to.x - from.x) / 2f;
            centerZ = from.z + (to.z - from.z) / 2f;
        } else {
            from = new BlockPos((int) (centerX - (to.x - centerX)), from.y, (int) (centerZ - (to.z - centerZ)));
        }

        double radiusX = Math.abs(to.x - centerX);
        double radiusZ = Math.abs(to.z - centerZ);

        if (fill)
            addCircleBlocks(list, from.x, from.y, from.z, to.x, to.y, to.z, centerX, centerZ, radiusX, radiusZ);
        else
            addHollowCircleBlocks(list, from.x, from.y, from.z, to.x, to.y, to.z, centerX, centerZ, radiusX, radiusZ);

        return list;
    }

    public static void addCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, double centerX, double centerZ, double radiusX, double radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {
            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {
                double distance = distance(l, n, centerX, centerZ);
                double radius = calculateEllipseRadius(centerX, centerZ, radiusX, radiusZ, l, n);
                if (distance < radius + 0.4f)
                    list.add(new BlockPos(l, y1, n));
            }
        }
    }

    public static void addHollowCircleBlocks(List<BlockPos> list, int x1, int y1, int z1, int x2, int y2, int z2, double centerX, double centerZ, double radiusX, double radiusZ) {
        for (int l = x1; x1 < x2 ? l <= x2 : l >= x2; l += x1 < x2 ? 1 : -1) {
            for (int n = z1; z1 < z2 ? n <= z2 : n >= z2; n += z1 < z2 ? 1 : -1) {
                double distance = distance(l, n, centerX, centerZ);
                double radius = calculateEllipseRadius(centerX, centerZ, radiusX, radiusZ, l, n);
                if (distance < radius + 0.4f && distance > radius - 0.6f)
                    list.add(new BlockPos(l, y1, n));
            }
        }
    }

    private static double distance(double x1, double z1, double x2, double z2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1));
    }

    public static double calculateEllipseRadius(double centerX, double centerZ, double radiusX, double radiusZ, int x, int z) {
        //https://math.stackexchange.com/questions/432902/how-to-get-the-radius-of-an-ellipse-at-a-specific-angle-by-knowing-its-semi-majo
        double theta = Math.atan2(z - centerZ, x - centerX);
        double part1 = radiusX * radiusX * Math.sin(theta) * Math.sin(theta);
        double part2 = radiusZ * radiusZ * Math.cos(theta) * Math.cos(theta);
        return radiusX * radiusZ / Math.sqrt(part1 + part2);
    }
    
}
