package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.logging.LogUtils;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.function.Supplier;

public class DataGenerators {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final GeneratorMap<BlockModelGenerators> blockModelGenerators = new GeneratorMap<>();
    private static final GeneratorMap<ItemModelGenerators> itemModelGenerators = new GeneratorMap<>();
    private static final GeneratorMap<DTDataProvider.Language> langGenerators = new GeneratorMap<>();

    public static void register(){
        registerBlockModelGenerator(Family.class, DynamicTrees.location("branch"), BranchStateGenerator::new);
        registerBlockModelGenerator(Family.class, DynamicTrees.location("stripped_branch"), StrippedBranchStateGenerator::new);
        registerBlockModelGenerator(Family.class, DynamicTrees.location("surface_root"), SurfaceRootStateGenerator::new);
        registerBlockModelGenerator(Family.class, DynamicTrees.location("roots"), RootsStateGenerator::new);
        registerItemModelGenerator(Family.class, DynamicTrees.location("branch_item"), BranchItemModelGenerator::new);
        registerItemModelGenerator(Family.class, DynamicTrees.location("roots_item"), RootsItemModelGenerator::new);
        registerLangGenerator(Family.class, DynamicTrees.location("family_lang"), FamilyLangGenerator::new);

        registerBlockModelGenerator(Species.class, DynamicTrees.location("sapling"), SaplingStateGenerator::new);
        registerItemModelGenerator(Species.class, DynamicTrees.location("seed_item"), SeedItemModelGenerator::new);
        registerLangGenerator(Species.class, DynamicTrees.location("species_lang"), SpeciesLangGenerator::new);

        registerBlockModelGenerator(LeavesProperties.class, DynamicTrees.location("leaves"), LeavesStateGenerator::new);
        registerBlockModelGenerator(LeavesProperties.class, DynamicTrees.location("palm_fronds"), PalmLeavesStateGenerator::new);

        registerBlockModelGenerator(SoilProperties.class, DynamicTrees.location("soil"), SoilStateGenerator::new);
        registerBlockModelGenerator(SoilProperties.class, DynamicTrees.location("water_root_soil"), WaterRootSoilGenerator::new);
        registerBlockModelGenerator(SoilProperties.class, DynamicTrees.location("aerial_root_soil"), AerialRootSoilGenerator::new);

        registerBlockModelGenerator(Fruit.class, DynamicTrees.location("fruit"), FruitsStateGenerator::new);

    }

    public static <T extends RegistryEntry<?>> void registerBlockModelGenerator (Class<T> type, Identifier id, Supplier<Generator<BlockModelGenerators, T>> generator){
        blockModelGenerators.put(type, id, generator);
    }
    public static <T extends RegistryEntry<?>> void registerItemModelGenerator (Class<T> type, Identifier id, Supplier<Generator<ItemModelGenerators, T>> generator){
        itemModelGenerators.put(type, id, generator);
    }
    public static <T extends RegistryEntry<?>> void registerLangGenerator (Class<T> type, Identifier id, Supplier<Generator<DTDataProvider.Language, T>> generator){
        langGenerators.put(type, id, generator);
    }

    public static <T extends RegistryEntry<?>> void runBlockModelGenerator(BlockModelGenerators blockModels, T input, Identifier id) {
        var generator = blockModelGenerators.get(input, id);
        if (generator == null) {
            LOGGER.warn("Could not find block model generator {} for RegistryEntry {}.", id, input.getClass().getSimpleName());
            return;
        }
        generator.generate(blockModels, input);
    }
    public static <T extends RegistryEntry<?>> void runItemModelGenerator(ItemModelGenerators itemModels, T input, Identifier id) {
        var generator = itemModelGenerators.get(input, id);
        if (generator == null) {
            LOGGER.warn("Could not find item model generator {} for RegistryEntry {}.", id, input.getClass().getSimpleName());
            return;
        }
        generator.generate(itemModels, input);
    }

    public static <T extends RegistryEntry<?>, L extends DTDataProvider.Language> void runLangGenerator(L langProvider, T input, Identifier id) {
        var generator = langGenerators.get(input, id);
        if (generator == null) {
            LOGGER.warn("Could not find language generator {} for RegistryEntry {}.", id, input.getClass().getSimpleName());
            return;
        }
        generator.generate(langProvider, input);
    }

    static class GeneratorMap <G> {
        private record Key(Class<? extends RegistryEntry<?>> type, Identifier id) {}

        final HashMap<Key, Supplier<Generator<G, ?>>> map = new HashMap<>();

        @SuppressWarnings("unchecked")
        public <T extends RegistryEntry<?>> void put (Class<T> type, Identifier id, Supplier<Generator<G, T>> generator){
            Key key = new Key(type, id);
            map.put(key, (Supplier<Generator<G, ?>>)(Supplier<?>)generator);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T extends RegistryEntry<?>> Generator<G, T> get(Class<T> type, Identifier id) {
            Supplier<Generator<G, ?>> supplier = map.get(new Key(type, id));
            return supplier == null ? null : (Generator<G, T>) supplier.get();
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T extends RegistryEntry<?>> Generator<G, T> get(T instance, Identifier id) {
            return get((Class<T>)instance.getRegistryType(), id);
        }
    }

}
