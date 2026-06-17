package net.mellow.effortless.api;

import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/**
 * The registry for whitelisting valid blocks for the gadgets to work with.
 * 
 * Make sure the block item extends `ItemBlock`, and that it doesn't place more blocks outside of itself.
 * 
 * @author Mellow
 */
public class BlockRegistry {

    private static final Set<Block> whitelist = new HashSet<>();

    /**
     * Adds a block to the whitelist, allowing the gadget to work with it.
     * 
     * NULLs are discarded, make sure the block itself is registered in the game registry
     * BEFORE trying to register it here. Safest place to call this is probably your `postInit`.
     * 
     * @param block the block to whitelist
     */
    public static void addToWhitelist(Block block) {
        if (block == null) return;
        whitelist.add(block);
    }

    /**
     * Adds a block to the whitelist, allowing the gadget to work with it.
     * 
     * @param modid the string identifier of the mod the block is registered by
     * @param name  the string identifier of the block itself
     */
    public static void addToWhitelist(String modid, String name) {
        addToWhitelist(GameRegistry.findBlock(modid, name));
    }

    /**
     * Checks whether a block has been added to the explicit whitelist.
     * 
     * Does not list blocks that work natively with the tool, like non-TE blocks.
     * 
     * @param block the block to check against
     * @return      whether or not the block is whitelisted
     */
    public static boolean isWhitelisted(Block block) {
        return whitelist.contains(block);
    }
    
}
