package net.mellow.effortless.compat;

import net.mellow.effortless.api.BlockRegistry;

public class Compat {

    public static final String MODID_NTM = "hbm";
    public static final String MODID_COFH = "CoFHAPI";
    public static final String MODID_BAUBLES = "Baubles|Expanded";
    public static final String MODID_AE2 = "appliedenergistics2";

    public static void register() {

        // ArchitectureCraft
        BlockRegistry.addToWhitelist("ArchitectureCraft", "shape");
        BlockRegistry.addToWhitelist("ArchitectureCraft", "shapeSE");

        // Carpenter's Blocks
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersBlock");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersBarrier");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersButton");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersGate");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersLadder");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersPressurePlate");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersSlope");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersStairs");
        BlockRegistry.addToWhitelist("CarpentersBlocks", "blockCarpentersTorch");

    }

}
