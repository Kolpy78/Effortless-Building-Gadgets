package net.mellow.effortless;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.mellow.effortless.events.ClientEvents;
import net.mellow.effortless.items.IItemRenderPreview;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        IItemRenderPreview.init();

        Keybinds.register();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ClientEvents.init();
    }

}
