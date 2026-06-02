package net.mellow.effortless;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;

public class Keybinds {

    public static final String category = "effortless.key";

    public static KeyBinding uiKey = new KeyBinding(category + ".uiKey", Keyboard.KEY_LMENU, category);

    public static void register() {
        ClientRegistry.registerKeyBinding(uiKey);
    }
    
}
