package net.mellow.effortless.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IItemControlReceiver {
    
    public void receiveControl(EntityPlayer player, ItemStack stack, NBTTagCompound nbt);

}
