package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPropertiesManager;
import com.ferreusveritas.dynamictrees.trees.FamilyManager;
import com.ferreusveritas.dynamictrees.trees.SpeciesManager;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabaseManager;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Holds and registers data pack entries ({@link IFutureReloadListener} objects).
 *
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public final class DTResourceRegistries {

    public static final ResourceLocation RESOURCE_LOCATION = DynamicTrees.resLoc("registry_name");

    public static final String TREES = "trees";

    public static final TreesResourceManager TREES_RESOURCE_MANAGER = new TreesResourceManager();

    private static FamilyManager familyManager;
    private static LeavesPropertiesManager leavesPropertiesManager;
    private static SpeciesManager speciesManager;
    private static JoCodeManager joCodeManager;
    private static BiomeDatabaseManager biomeDatabaseManager;

    public static void setupTreesResourceManager () {
        leavesPropertiesManager = new LeavesPropertiesManager();
        familyManager = new FamilyManager();
        speciesManager = new SpeciesManager();
        joCodeManager = new JoCodeManager();
        biomeDatabaseManager = new BiomeDatabaseManager();

        TREES_RESOURCE_MANAGER.addReloadListeners(leavesPropertiesManager, familyManager, speciesManager, joCodeManager, biomeDatabaseManager);

        // Create and fire event so add-ons can register load listeners for custom tree resources.
        final AddTreesLoadListenerEvent addLoadListenerEvent = new AddTreesLoadListenerEvent(TREES_RESOURCE_MANAGER);
        ModLoader.get().postEvent(addLoadListenerEvent);

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

    public static FamilyManager getTreeFamilyManager() {
        return familyManager;
    }

    public static LeavesPropertiesManager getLeavesPropertiesManager() {
        return leavesPropertiesManager;
    }

    public static SpeciesManager getSpeciesManager() {
        return speciesManager;
    }

    public static JoCodeManager getJoCodeManager() {
        return joCodeManager;
    }

    public static BiomeDatabaseManager getBiomeDatabaseManager() {
        return biomeDatabaseManager;
    }

    public static final class AddTreesLoadListenerEvent extends Event implements IModBusEvent {
        private final TreesResourceManager treesResourceManager;

        public AddTreesLoadListenerEvent(final TreesResourceManager treesResourceManager) {
            this.treesResourceManager = treesResourceManager;
        }

        public TreesResourceManager getTreesResourceManager() {
            return treesResourceManager;
        }
    }

    @SubscribeEvent
    public static void addReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new ReloadTreesResources());
    }

    public static final class ReloadTreesResources implements IFutureReloadListener {
        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            // Reload all reload listeners in the trees resource manager.
            return CompletableFuture.runAsync(() -> TREES_RESOURCE_MANAGER.reload(stage, backgroundExecutor, gameExecutor));
        }
    }

}
