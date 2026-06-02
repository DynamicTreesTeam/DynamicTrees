package com.dtteam.dynamictrees.treepack;

import com.dtteam.dynamictrees.*;
import com.dtteam.dynamictrees.api.configuration.*;
import com.dtteam.dynamictrees.api.resource.*;
import com.dtteam.dynamictrees.platform.*;
import com.dtteam.dynamictrees.systems.genfeature.*;
import com.dtteam.dynamictrees.systems.growthlogic.*;
import com.dtteam.dynamictrees.treepack.loader.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.*;
import net.minecraft.world.item.crafting.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Harley O'Connor
 */
public final class Resources {

    public static final String TREES = "trees";

    public static final TreeResourceManager MANAGER = new TreesResourceManager();

    public static final LeavesPropertiesResourceLoader LEAVES_PROPERTIES_LOADER = new LeavesPropertiesResourceLoader();
    public static final SoilPropertiesResourceLoader SOIL_PROPERTIES_LOADER = new SoilPropertiesResourceLoader();
    public static final FamilyResourceLoader FAMILY_LOADER = new FamilyResourceLoader();

    public static final ConfigurationTemplateResourceLoader<GenFeatureConfiguration, GenFeature>
            GEN_FEATURE_TEMPLATE_LOADER = new ConfigurationTemplateResourceLoader<>(
            "gen_features/configurations",
            GenFeature.REGISTRY,
            GenFeatureConfiguration.TEMPLATES
    );

    public static final ConfigurationTemplateResourceLoader<GrowthLogicKitConfiguration, GrowthLogicKit>
            GROWTH_LOGIC_KIT_TEMPLATE_LOADER = new ConfigurationTemplateResourceLoader<>(
            "growth_logic_kits/configurations",
            GrowthLogicKit.REGISTRY,
            GrowthLogicKitConfiguration.TEMPLATES
    );

    public static final FruitResourceLoader FRUIT_LOADER = new FruitResourceLoader();
    public static final PodResourceLoader POD_LOADER = new PodResourceLoader();

    public static final SpeciesResourceLoader SPECIES_LOADER = new SpeciesResourceLoader();

    public static final JoCodeResourceLoader JO_CODE_LOADER = new JoCodeResourceLoader();
    public static final FeatureCancellationResourceLoader FEATURE_CANCELLATION_LOADER = new FeatureCancellationResourceLoader();
    public static final BiomePopulatorsResourceLoader BIOME_POPULATORS_LOADER = new BiomePopulatorsResourceLoader();

    public static void setupTreesResourceManager() {
        Services.EVENT.postAddResourceLoadersEventPre(MANAGER);
        addDefaultLoaders();
        Services.EVENT.postAddResourceLoadersEventPost(MANAGER);
        MANAGER.registerAppliers();

        registerModTreePacks();
        registerFlatTreePack();

        DynamicTrees.LOG.debug("Successfully loaded {} tree packs.", MANAGER.listPacks().count());
    }

    private static void addDefaultLoaders() {
        MANAGER.addLoaders(
                LEAVES_PROPERTIES_LOADER,
                SOIL_PROPERTIES_LOADER,
                FAMILY_LOADER,
                GEN_FEATURE_TEMPLATE_LOADER,
                GROWTH_LOGIC_KIT_TEMPLATE_LOADER,
                FRUIT_LOADER,
                POD_LOADER,
                SPECIES_LOADER,
                JO_CODE_LOADER,
                FEATURE_CANCELLATION_LOADER,
                BIOME_POPULATORS_LOADER
        );
    }

    private static void registerModTreePacks() {
        // Register all mod tree packs. Gets the mods in an ordered list so that add-ons will come after DT.
        // This means that add-ons will take priority over DT.
        Services.PLATFORM.getMods().forEach(Resources::addModTreePack);
    }

    private static void addModTreePack(ModFileContainer modFile) {
        final Optional<Path> treesPath = modFile.findResource(TREES);
        if (treesPath.isEmpty()) {
            DynamicTrees.LOG.debug("No Tree Pack found for mod {}", modFile.getModId());
            return;
        }
        final Path absTreesPath = treesPath.get().toAbsolutePath();

        if (Files.exists(absTreesPath)) {
            MANAGER.addPack(new TreePackResources(
                    new PackLocationInfo(
                            modFile.getModId(),
                            Component.translatable("treePack."+modFile.getModId()+".name"),
                            PackSource.WORLD,
                            Optional.empty()),
                    absTreesPath.toAbsolutePath()
            ));
        }
    }

    private static final PackLocationInfo FLAT_TREE_PACK_INFO = new PackLocationInfo(
            "dynamictrees", Component.translatable("treePack.dynamictrees.name"), PackSource.BUILT_IN, Optional.empty()
    );
    private static void registerFlatTreePack() {
        final File mainTreeFolder = getTreeFolder();
        MANAGER.addPack(new TreePackResources(FLAT_TREE_PACK_INFO, mainTreeFolder.toPath().toAbsolutePath()));
    }

    private static File getTreeFolder() {
        final File mainTreeFolder = new File("trees/");

        // Create the trees folder if it doesn't already exist, crash if failed.
        if (!mainTreeFolder.exists() && !mainTreeFolder.mkdir()) {
            throw new RuntimeException("Failed to create \"trees\" folder in your Minecraft directory.");
        }
        return mainTreeFolder;
    }

    /**
     * Listens for datapack reloads for actions such as reloading the trees resource manager and registering dirt bucket
     * recipes.
     */
    public static class ReloadListener implements PreparableReloadListener {
        private RecipeManager recipeManager;

        public ReloadListener(RecipeManager recipeManager) {
            this.recipeManager = recipeManager;
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager,
                                              ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                              Executor backgroundExecutor, Executor gameExecutor) {
            final CompletableFuture<?>[] futures = MANAGER.prepareReload(gameExecutor, backgroundExecutor);

            // Reload all reload listeners in the trees resource manager and registers dirt bucket recipes.
            return CompletableFuture.allOf(futures)
                    .thenCompose(stage::wait)
                    .thenAcceptAsync(v -> MANAGER.reload(futures), gameExecutor);
        }

    }

}
