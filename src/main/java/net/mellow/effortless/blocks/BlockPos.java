package net.mellow.effortless.blocks;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class BlockPos implements Comparable<BlockPos> {

    public int x;
    public int y;
    public int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockPos containing(Vec3 vec) {
        return new BlockPos(MathHelper.floor_double(vec.xCoord), MathHelper.floor_double(vec.yCoord), MathHelper.floor_double(vec.zCoord));
    }

    public BlockPos subtract(BlockPos pos) {
        return new BlockPos(x - pos.x, y - pos.y, z - pos.z);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BlockPos other = (BlockPos) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (z != other.z)
            return false;
        return true;
    }

    @Override
    public int compareTo(BlockPos o) {
        return equals(o) ? 0 : 1;
    }

    @Override
    public String toString() {
        return x + "x" + y + "x" + z;
    }

}
