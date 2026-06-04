package net.mellow.effortless.items;

import java.util.List;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import api.hbm.energymk2.IBatteryItem;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cofh.api.energy.IEnergyContainerItem;
import net.mellow.effortless.Keybinds;
import net.mellow.effortless.blocks.BlockMeta;
import net.mellow.effortless.buildmode.BaseBuildMode;
import net.mellow.effortless.buildmode.BuildModes;
import net.mellow.effortless.buildmode.History;
import net.mellow.effortless.buildmode.modes.*;
import net.mellow.effortless.gui.GuiBuildingGadget;
import net.mellow.effortless.network.IItemControlReceiver;
import net.mellow.effortless.util.MathUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

@Optional.InterfaceList({
    @Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI"),
    @Optional.Interface(iface = "api.hbm.energymk2.IBatteryItem", modid = "hbm"),
})
public class ItemBuildingGadget extends Item implements IItemRenderPreview, IItemGuiProvider, IItemControlReceiver, IEnergyContainerItem, IBatteryItem {

    public static enum BuildingMode {
        EXTENDED(new Extended(), 16, 16), // greater reach
        AIR(new Air(), 240, 16), // air placement
        LINE(new Line(), 32, 16), // lines
        WALL(new Wall(), 48, 16), // walls
        FLOOR(new Floor(), 64, 16), // floors
        CUBE(new Cube(), 80, 16); // miney crafta

        public final BaseBuildMode handler;
        public final int iconX;
        public final int iconY;

        private BuildingMode(BaseBuildMode handler, int iconX, int iconY) {
            this.handler = handler;
            this.iconX = iconX;
            this.iconY = iconY;
        }
        
        public String getUnlocalizedName() {
            return "buildingmode." + name().toLowerCase(Locale.ROOT) + ".name";
        }

        public String getUnlocalizedDesc() {
            return "buildingmode." + name().toLowerCase(Locale.ROOT) + ".desc";
        }
    }

    private static boolean hasRF;
    private static boolean hasHE;

    static {
        try {
            Class.forName("cofh.api.energy.IEnergyContainerItem");
            hasRF = true;
        } catch (Exception ex) {
            hasRF = false;
        }
        hasHE = Loader.isModLoaded("hbm");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool) {
        EnumChatFormatting chargeFormat = getEnergyStored(stack) >= capacity / 10 ? EnumChatFormatting.BLUE : EnumChatFormatting.RED;
        if (hasRF) list.add(chargeFormat + I18n.format("energy.stored.rf", MathUtil.getShortNumber(getEnergyStored(stack)), MathUtil.getShortNumber(getMaxEnergyStored(stack))));
        if (hasHE) list.add(chargeFormat + I18n.format("energy.stored.he", MathUtil.getShortNumber(getCharge(stack)), MathUtil.getShortNumber(getMaxCharge(stack))));
        list.add(EnumChatFormatting.YELLOW + I18n.format("hint.uikey.usage", Keyboard.getKeyName(Keybinds.uiKey.getKeyCode())));
    }

    // IF WE PUT ROCKS IN THE SHAPE OF A RUNWAY GOD WILL GIVE US HIGH-FRUCTOSE CORN SYRUP
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
        BuildingMode mode = getMode(stack);

        MovingObjectPosition mop = BuildModes.getMop(player, mode.handler.reach(stack));

        boolean requiresPower = !player.capabilities.isCreativeMode && (hasRF || hasHE);
        int energy = stack.stackTagCompound.getInteger("energy");

        if (requiresPower) {
            // require 10% charge to operate
            if (energy < capacity / 10) return stack;
        }

        int blocksPlaced = mode.handler.add(stack, getSelected(stack), world, player, mop);

        if (requiresPower) {
            stack.stackTagCompound.setInteger("energy", Math.max(0, energy - blocksPlaced * consumption));
        }

        return stack;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        getMode(stack).handler.clear(stack);
        return false;
    }


    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return (hasRF || hasHE) && getEnergyStored(stack) < getMaxEnergyStored(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - (double) getEnergyStored(stack) / (double) getMaxEnergyStored(stack);
    }


    public static BlockMeta getSelected(ItemStack stack) {
        if (stack.stackTagCompound == null) return new BlockMeta(Blocks.stone, 0);
        return new BlockMeta(stack.stackTagCompound.getInteger("block"), stack.stackTagCompound.getByte("meta"));
    }


    // mode is stored as a string so inserting new modes won't fuck up existing tools
    public static BuildingMode getMode(ItemStack stack) {
        if (stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("mode")) return BuildingMode.EXTENDED;
        try {
            return BuildingMode.valueOf(stack.stackTagCompound.getString("mode"));
        } catch (IllegalArgumentException ex) {
            return BuildingMode.LINE;
        }
    }


    @Override
    public void render(World world, EntityPlayer player, ItemStack stack, float partialTicks) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
        getMode(stack).handler.render(stack, world, player, partialTicks);
    }

    @Override
    public void provideGui(ItemStack stack, EntityPlayer player) {
        FMLCommonHandler.instance().showGuiScreen(new GuiBuildingGadget(stack));
    }

    @Override
    public void receiveControl(EntityPlayer player, ItemStack stack, NBTTagCompound nbt) {
        getMode(stack).handler.clear(stack);

        if (nbt.hasKey("mode")) stack.stackTagCompound.setString("mode", nbt.getString("mode"));
        if (nbt.hasKey("block")) stack.stackTagCompound.setInteger("block", nbt.getInteger("block"));
        if (nbt.hasKey("meta")) stack.stackTagCompound.setByte("meta", nbt.getByte("meta"));

        if (nbt.hasKey("action")) {
            String action = nbt.getString("action");

            switch (action) {
                case "undo": History.undo(player.worldObj, player); break;
                case "redo": History.redo(player.worldObj, player); break;
            }
        }
    }

    // POWERRRRRR
    private int capacity = 1_000_000;
    private int consumption = 100;


    /// FE ///
    @Override
    public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
        int energy = stack.stackTagCompound.getInteger("energy");
        int energyReceived = Math.min(capacity - energy, maxReceive);

        if (!simulate) {
            energy += energyReceived;
            stack.stackTagCompound.setInteger("energy", energy);
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(ItemStack stack, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(ItemStack stack) {
        if (stack.stackTagCompound == null) return 0;
        return stack.stackTagCompound.getInteger("energy");
    }

    @Override
    public int getMaxEnergyStored(ItemStack stack) {
        return capacity;
    }
    /// /FE ///


    /// HE ///
    @Override
    public void chargeBattery(ItemStack stack, long power) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
        int energy = stack.stackTagCompound.getInteger("energy");
        energy += Math.max(1, (int) (power / 5));
        stack.stackTagCompound.setInteger("energy", energy);
    }

    @Override
    public void setCharge(ItemStack stack, long power) {
        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();
        stack.stackTagCompound.setInteger("energy", (int) (power / 5));
    }

    @Override
    public void dischargeBattery(ItemStack stack, long energy) {
        
    }

    @Override
    public long getCharge(ItemStack stack) {
        if (stack.stackTagCompound == null) return 0;
        return stack.stackTagCompound.getInteger("energy") * 5;
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        return capacity * 5;
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        return 10_000;
    }

    @Override
    public long getDischargeRate(ItemStack stack) {
        return 0;
    }
    /// /HE ///

}
