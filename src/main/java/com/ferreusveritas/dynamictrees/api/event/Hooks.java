package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.api.resource.TreeResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;

import static com.ferreusveritas.dynamictrees.worldgen.BiomeDatabases.getDefault;
import static com.ferreusveritas.dynamictrees.worldgen.BiomeDatabases.getDimensionalDatabases;

/**
 * @author Harley O'Connor
 */
public final class Hooks {

    public static void onAddResourceLoaders(TreeResourceManager resourceManager) {
        ModLoader.get().postEvent(new AddResourceLoadersEvent(resourceManager));
    }

    public static void onAddFeatureCancellers() {
        ModLoader.get().postEvent(new AddFeatureCancellersEvent(getDefault()));
    }

    public static void onPopulateDefaultDatabase() {
        MinecraftForge.EVENT_BUS.post(new PopulateDefaultDatabaseEvent(getDefault()));
    }

    public static void onPopulateDimensionalDatabases() {
        MinecraftForge.EVENT_BUS.post(new PopulateDimensionalDatabaseEvent(getDimensionalDatabases(), getDefault()));
    }

}
