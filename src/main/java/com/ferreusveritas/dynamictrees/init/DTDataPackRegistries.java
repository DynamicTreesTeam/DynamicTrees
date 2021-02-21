package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.SpeciesManager;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabaseManager;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeManager;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Holds and registers data pack entries ({@link IFutureReloadListener} objects).
 *
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public final class DTDataPackRegistries {

    public static final JoCodeManager JO_CODE_MANAGER = new JoCodeManager();
    public static final BiomeDatabaseManager BIOME_DATABASE_MANAGER = new BiomeDatabaseManager();
    public static final SpeciesManager SPECIES_MANAGER = new SpeciesManager();

    @SubscribeEvent
    public static void onAddReloadListeners (final AddReloadListenerEvent event) {
        event.addListener(JO_CODE_MANAGER);
        event.addListener(BIOME_DATABASE_MANAGER);
    }

}
