package net.mellow.effortless;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean consumesEnergy = true;
    public static int capacityRF = 1_000_000;
    public static int consumptionRF = 20;
    public static float conversionHEtoRF = 5;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        consumesEnergy = configuration.getBoolean("consumesEnergy", Configuration.CATEGORY_GENERAL, consumesEnergy, "Should the gadget require energy to function? Defaults to false if no RF/HE energy compatible mod is detected.");
        capacityRF = configuration.getInt("capacityRF", Configuration.CATEGORY_GENERAL, capacityRF, 1, Integer.MAX_VALUE, "How much energy should the gadget store (in RF)?");
        consumptionRF = configuration.getInt("consumptionRF", Configuration.CATEGORY_GENERAL, consumptionRF, 1, Integer.MAX_VALUE, "How much energy should the gadget use per block placed (in RF)?");
        conversionHEtoRF = configuration.getFloat("conversionHEtoRF", Configuration.CATEGORY_GENERAL, conversionHEtoRF, Float.MIN_NORMAL, Float.MAX_VALUE, "Conversion rate of HE into RF.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
