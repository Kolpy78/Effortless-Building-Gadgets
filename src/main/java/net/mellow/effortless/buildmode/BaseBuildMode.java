package net.mellow.effortless.buildmode;

import org.lwjgl.opengl.GL11;

import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.blocks.BlockPos;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public abstract class BaseBuildMode {
    
    public abstract void add(ItemStack stack, BlockMeta selected, World world, EntityPlayer player, MovingObjectPosition mop);
    public abstract void clear(ItemStack stack);

    public int reach(ItemStack stack) {
        return 32;
    }

    public static void buildBox(World world, BlockMeta selected, BlockPos from, BlockPos to) {
        if (from == null || to == null) return;
        for (int x = Math.min(from.x, to.x); x <= Math.max(from.x, to.x); x++)
        for (int y = Math.min(from.y, to.y); y <= Math.max(from.y, to.y); y++)
        for (int z = Math.min(from.z, to.z); z <= Math.max(from.z, to.z); z++) {
            world.setBlock(x, y, z, selected.block, selected.meta, 3);
        }
    }

    public abstract void render(ItemStack stack, World world, EntityPlayer player, float partialTicks);

    public static void renderBox(EntityPlayer player, float partialTicks, BlockPos from, BlockPos to) {
        double dx = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double dy = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double dz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

        double minX = Math.min(from.x, to.x) + 0.125;
        double maxX = Math.max(from.x, to.x) + 0.875;
        double minY = Math.min(from.y, to.y) + 0.125;
        double maxY = Math.max(from.y, to.y) + 0.875;
        double minZ = Math.min(from.z, to.z) + 0.125;
        double maxZ = Math.max(from.z, to.z) + 0.875;
        
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(1F, 1F, 1F);
        
        Tessellator tess = Tessellator.instance;
        tess.setTranslation(-dx, -dy, -dz);
        tess.startDrawing(GL11.GL_LINES);
        tess.setBrightness(240);
        tess.setColorRGBA_F(1F, 1F, 1F, 1F);
        
        // top
        tess.addVertex(minX, maxY, minZ);
        tess.addVertex(minX, maxY, maxZ);
        
        tess.addVertex(minX, maxY, maxZ);
        tess.addVertex(maxX, maxY, maxZ);
        
        tess.addVertex(maxX, maxY, maxZ);
        tess.addVertex(maxX, maxY, minZ);

        tess.addVertex(maxX, maxY, minZ);
        tess.addVertex(minX, maxY, minZ);
        
        // bottom
        tess.addVertex(minX, minY, minZ);
        tess.addVertex(minX, minY, maxZ);
        
        tess.addVertex(minX, minY, maxZ);
        tess.addVertex(maxX, minY, maxZ);
        
        tess.addVertex(maxX, minY, maxZ);
        tess.addVertex(maxX, minY, minZ);

        tess.addVertex(maxX, minY, minZ);
        tess.addVertex(minX, minY, minZ);

        // sides
        tess.addVertex(minX, minY, minZ);
        tess.addVertex(minX, maxY, minZ);

        tess.addVertex(maxX, minY, minZ);
        tess.addVertex(maxX, maxY, minZ);

        tess.addVertex(maxX, minY, maxZ);
        tess.addVertex(maxX, maxY, maxZ);

        tess.addVertex(minX, minY, maxZ);
        tess.addVertex(minX, maxY, maxZ);
        
        tess.draw();
        tess.setTranslation(0, 0, 0);
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

}
