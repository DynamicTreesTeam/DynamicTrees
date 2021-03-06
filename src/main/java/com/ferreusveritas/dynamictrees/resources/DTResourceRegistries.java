package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPropertiesManager;
import com.ferreusveritas.dynamictrees.trees.SpeciesManager;
import com.ferreusveritas.dynamictrees.trees.TreeFamilyManager;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabaseManager;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeManager;
import com.google.common.collect.Lists;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Holds and registers data pack entries ({@link IFutureReloadListener} objects).
 *
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public final class DTResourceRegistries {

    ///////////////////////////////////////////
    // Tree packs
    ///////////////////////////////////////////

    public static final String TREES = "trees";

    public static final TreesResourceManager TREES_RESOURCE_MANAGER = new TreesResourceManager();

    private static TreeFamilyManager treeFamilyManager;
    private static LeavesPropertiesManager leavesPropertiesManager;

    public static void setupTreesResourceManager () {
        leavesPropertiesManager = new LeavesPropertiesManager();
        treeFamilyManager = new TreeFamilyManager();
        TREES_RESOURCE_MANAGER.addLoadListeners(leavesPropertiesManager, treeFamilyManager);

        // Create and fire event so add-ons can register load listeners for custom tree resources.
        final AddTreesLoadListenerEvent addLoadListenerEvent = new AddTreesLoadListenerEvent(TREES_RESOURCE_MANAGER);
        MinecraftForge.EVENT_BUS.post(addLoadListenerEvent);

        ModList.get().getMods().forEach(modInfo -> {
            final String modId = modInfo.getModId();
            final IModFile modFile = ModList.get().getModFileById(modId).getFile();

            if (modId.equals(DynamicTrees.MOD_ID) || !modFile.getLocator().isValid(modFile))
                return;

            registerModTreePack(modFile);
        });

        // Add dynamic trees last so other add-ons take priority.
        registerModTreePack(ModList.get().getModFileById(DynamicTrees.MOD_ID).getFile());

        LogManager.getLogger().debug("Successfully loaded " + TREES_RESOURCE_MANAGER.getResourcePackStream().count() + " tree packs.");
    }

    private static void registerModTreePack (IModFile modFile) {
        final Path treesPath = modFile.getLocator().findPath(modFile, TREES).toAbsolutePath();

        // Only add resource pack if the trees file exists in the mod file.
        if (Files.exists(treesPath))
            TREES_RESOURCE_MANAGER.addResourcePack(new ModTreeResourcePack(treesPath, modFile));
    }

    public static TreeFamilyManager getTreeFamilyManager() {
        return treeFamilyManager;
    }

    public static LeavesPropertiesManager getLeavesPropertiesManager() {
        return leavesPropertiesManager;
    }

    public static final class AddTreesLoadListenerEvent extends Event {

        private final TreesResourceManager treesResourceManager;

        public AddTreesLoadListenerEvent(final TreesResourceManager treesResourceManager) {
            this.treesResourceManager = treesResourceManager;
        }

        public TreesResourceManager getTreesResourceManager() {
            return treesResourceManager;
        }

    }

    ///////////////////////////////////////////
    // Data packs
    ///////////////////////////////////////////

    public static final JoCodeManager JO_CODE_MANAGER = new JoCodeManager();
    public static final BiomeDatabaseManager BIOME_DATABASE_MANAGER = new BiomeDatabaseManager();
    public static final SpeciesManager SPECIES_MANAGER = new SpeciesManager();

    @SubscribeEvent
    public static void onAddReloadListeners (final AddReloadListenerEvent event) {
        event.addListener(SPECIES_MANAGER);
        event.addListener(JO_CODE_MANAGER);
        event.addListener(BIOME_DATABASE_MANAGER);
    }

}
