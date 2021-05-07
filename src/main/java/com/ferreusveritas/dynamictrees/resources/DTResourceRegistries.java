package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPropertiesManager;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRecipes;
import com.ferreusveritas.dynamictrees.trees.FamilyManager;
import com.ferreusveritas.dynamictrees.trees.SpeciesManager;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabaseManager;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeManager;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
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
import java.util.HashMap;
import java.util.Map;
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

    public static final LeavesPropertiesManager LEAVES_PROPERTIES_MANAGER = new LeavesPropertiesManager();
    public static final FamilyManager FAMILY_MANAGER = new FamilyManager();
    public static final SpeciesManager SPECIES_MANAGER = new SpeciesManager();
    public static final JoCodeManager JO_CODE_MANAGER = new JoCodeManager();
    public static final BiomeDatabaseManager BIOME_DATABASE_MANAGER = new BiomeDatabaseManager();

    public static void setupTreesResourceManager () {
        TREES_RESOURCE_MANAGER.addReloadListeners(LEAVES_PROPERTIES_MANAGER, FAMILY_MANAGER, SPECIES_MANAGER, JO_CODE_MANAGER, BIOME_DATABASE_MANAGER);

        // Create and fire event so add-ons can register load listeners for custom tree resources.
        final AddTreesLoadListenerEvent addLoadListenerEvent = new AddTreesLoadListenerEvent(TREES_RESOURCE_MANAGER);
        ModLoader.get().postEvent(addLoadListenerEvent);

        TREES_RESOURCE_MANAGER.registerJsonAppliers(); // Register any Json appliers reload listeners.

        ModList.get().getMods().forEach(modInfo -> {
            final String modId = modInfo.getModId();
            final IModFile modFile = ModList.get().getModFileById(modId).getFile();

            if (modId.equals(DynamicTrees.MOD_ID) || !modFile.getLocator().isValid(modFile))
                return;

            registerModTreePack(modFile);
        });

        // Add dynamic trees last so other add-ons take priority.
        registerModTreePack(ModList.get().getModFileById(DynamicTrees.MOD_ID).getFile());

        LogManager.getLogger().debug("Successfully loaded " + TREES_RESOURCE_MANAGER.listPacks().count() + " tree packs.");
    }

    private static void registerModTreePack (IModFile modFile) {
        final Path treesPath = modFile.getLocator().findPath(modFile, TREES).toAbsolutePath();

        // Only add resource pack if the trees file exists in the mod file.
        if (Files.exists(treesPath))
            TREES_RESOURCE_MANAGER.addResourcePack(new ModTreeResourcePack(treesPath, modFile));
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
        event.addListener(new ReloadListener(event.getDataPackRegistries()));
    }

    /**
     * Listens for datapack reloads for actions such as reloading the trees resource
     * manager and registering dirt bucket recipes.
     */
    public static final class ReloadListener implements IFutureReloadListener {
        private final DataPackRegistries dataPackRegistries;

        public ReloadListener(DataPackRegistries dataPackRegistries) {
            this.dataPackRegistries = dataPackRegistries;
        }

        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            final CompletableFuture<?>[] futures = TREES_RESOURCE_MANAGER.prepareReload(backgroundExecutor, gameExecutor);

            // Reload all reload listeners in the trees resource manager and registers dirt bucket recipes.
            return CompletableFuture.allOf(futures)
                    .thenCompose(stage::wait)
                    .thenAcceptAsync(theVoid -> TREES_RESOURCE_MANAGER.reload(futures), gameExecutor)
                    .thenRunAsync(this::registerDirtBucketRecipes, gameExecutor);
        }

        private void registerDirtBucketRecipes() {
            if (!DTConfigs.GENERATE_DIRT_BUCKET_RECIPES.get())
                return;

            final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = new HashMap<>();

            // Put the recipes into the new map and make each type's recipes mutable.
            this.dataPackRegistries.getRecipeManager().recipes.forEach(((recipeType, currentRecipes) ->
                    recipes.put(recipeType, new HashMap<>(currentRecipes))));

            // Register dirt bucket recipes.
            DTRecipes.registerDirtBucketRecipes(recipes.get(IRecipeType.CRAFTING));

            // Revert each type's recipes back to immutable.
            recipes.forEach(((recipeType, currentRecipes) -> recipes.put(recipeType, ImmutableMap.copyOf(currentRecipes))));

            // Set the new recipes.
            dataPackRegistries.getRecipeManager().recipes = ImmutableMap.copyOf(recipes);
        }
    }

}
