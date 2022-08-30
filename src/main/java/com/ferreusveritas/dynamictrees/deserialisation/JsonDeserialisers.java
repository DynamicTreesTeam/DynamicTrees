package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.configurations.PropertyDefinition;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.SeedSaplingRecipe;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorConfiguration;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.Drops;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.function.BiomePredicate;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import com.ferreusveritas.dynamictrees.worldgen.deserialisation.ChanceSelectorDeserialiser;
import com.ferreusveritas.dynamictrees.worldgen.deserialisation.DensitySelectorDeserialiser;
import com.ferreusveritas.dynamictrees.worldgen.deserialisation.SpeciesSelectorDeserialiser;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Holds {@link JsonDeserialiser} objects, which can be used to obtain objects from {@link JsonElement} objects.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unused")
public final class JsonDeserialisers {

    private static final Map<Class<?>, JsonDeserialiser<?>> DESERIALISERS = Maps.newHashMap();

    private static final class NullDeserialiser<O> implements JsonDeserialiser<O> {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean deserialiseIfValid(JsonElement input, Consumer<Result<O, JsonElement>> consumer) {
            return false;
        }

        @Override
        public Result<O, JsonElement> deserialise(JsonElement input) {
            return JsonResult.failure(input, "Could not get Json deserialiser for json element: " + input + ".");
        }
    }

    /**
     * Returned by {@link #get(Class)} if an object getter wasn't found.
     */
    public static final JsonDeserialiser<?> NULL = new NullDeserialiser<>();

    /**
     * Gets the {@link JsonDeserialiser} for the given class type.
     *
     * @param type The {@link Class} of the object to get.
     * @param <T>  The type of the object.
     * @return The {@link JsonDeserialiser} for the class, or {@link #NULL} if it wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonDeserialiser<T> get(final Class<T> type) {
        return (JsonDeserialiser<T>) DESERIALISERS.getOrDefault(type, NULL);
    }

    public static <T> JsonDeserialiser<T> getOrThrow(final Class<T> type) throws NoSuchDeserialiserException {
        return getOrThrow(type, "No Json deserialiser found for type \"" + type.getName() + "\".");
    }

    @SuppressWarnings("unchecked")
    public static <T> JsonDeserialiser<T> getOrThrow(Class<T> type, String errorMessage)
            throws NoSuchDeserialiserException {
        final JsonDeserialiser<?> deserialiser = DESERIALISERS.get(type);
        if (deserialiser == null) {
            throw new NoSuchDeserialiserException(errorMessage);
        }
        return ((JsonDeserialiser<T>) deserialiser);
    }

    public static Set<Class<?>> getDeserialisableClasses() {
        return DESERIALISERS.keySet();
    }

    /**
     * Registers an {@link JsonDeserialiser} to the registry.
     *
     * @param outputClass  The {@link Class} of the object that will be obtained.
     * @param deserialiser The {@link JsonDeserialiser} to register.
     * @param <T>          The type of the object getter.
     * @return The {@link JsonDeserialiser} given.
     */
    public static <T> JsonDeserialiser register(final Class<T> outputClass, final JsonDeserialiser<T> deserialiser) {
        DESERIALISERS.put(outputClass, deserialiser);
        return deserialiser;
    }

    public static final JsonDeserialiser<JsonElement> JSON_ELEMENT = register(JsonElement.class, input ->
            JsonResult.success(input, input)
    );

    public static final JsonDeserialiser<JsonPrimitive> JSON_PRIMITIVE = register(JsonPrimitive.class, input ->
            input.isJsonPrimitive() ? JsonResult.success(input, input.getAsJsonPrimitive()) :
                    JsonResult.failure(input, "Json element was not a json primitive.")
    );

    public static final JsonDeserialiser<JsonObject> JSON_OBJECT = register(JsonObject.class, input ->
            input.isJsonObject() ? JsonResult.success(input, input.getAsJsonObject()) :
                    JsonResult.failure(input, "Json element was not a json object.")
    );

    public static final JsonDeserialiser<JsonArray> JSON_ARRAY = register(JsonArray.class, input ->
            input.isJsonArray() ? JsonResult.success(input, input.getAsJsonArray()) :
                    JsonResult.failure(input, "Json element was not a json array.")
    );

    public static final JsonDeserialiser<Boolean> BOOLEAN = register(Boolean.class, input ->
            JSON_PRIMITIVE.deserialise(input).mapIfValid(
                    JsonPrimitive::isBoolean,
                    "Could not get boolean from \"{}\".",
                    JsonPrimitive::getAsBoolean
            )
    );
    public static final JsonDeserialiser<Number> NUMBER = register(Number.class, input ->
            JSON_PRIMITIVE.deserialise(input).mapIfValid(
                    JsonPrimitive::isNumber,
                    "Could not get number from \"{}\".",
                    JsonPrimitive::getAsNumber
            )
    );
    public static final JsonDeserialiser<String> STRING = register(String.class, input ->
            JSON_PRIMITIVE.deserialise(input).mapIfValid(
                    JsonPrimitive::isString,
                    "Could not get string from \"{}\".",
                    JsonPrimitive::getAsString
            )
    );

    public static final JsonDeserialiser<Byte> BYTE = register(Byte.class, input ->
            NUMBER.deserialise(input).map(Number::byteValue)
    );
    public static final JsonDeserialiser<Short> SHORT = register(Short.class, input ->
            NUMBER.deserialise(input).map(Number::shortValue)
    );
    public static final JsonDeserialiser<Integer> INTEGER = register(Integer.class, input ->
            NUMBER.deserialise(input).map(Number::intValue)
    );
    public static final JsonDeserialiser<Long> LONG = register(Long.class, input ->
            NUMBER.deserialise(input).map(Number::longValue)
    );

    public static final JsonDeserialiser<Float> FLOAT = register(Float.class, input ->
            NUMBER.deserialise(input).map(Number::floatValue)
    );
    public static final JsonDeserialiser<Double> DOUBLE = register(Double.class, input ->
            NUMBER.deserialise(input).map(Number::doubleValue)
    );

    public static final JsonDeserialiser<ResourceLocation> RESOURCE_LOCATION =
            register(ResourceLocation.class, ResourceLocationDeserialiser.create());

    /**
     * Alternative to {@link #RESOURCE_LOCATION}, defaulting the namespace to {@code dynamictrees}.
     */
    public static final JsonDeserialiser<ResourceLocation> DT_RESOURCE_LOCATION =
            ResourceLocationDeserialiser.create(DynamicTrees.MOD_ID);

    public static JsonDeserialiser<Block> BLOCK;
    public static JsonDeserialiser<Item> ITEM;
    public static JsonDeserialiser<Biome> BIOME;

    // TODO: Read json object for quantity and NBT.
    public static JsonDeserialiser<ItemStack> ITEM_STACK = register(ItemStack.class,
            input -> ITEM.deserialise(input).map((Result.SimpleMapper<Item, ItemStack>) ItemStack::new));

    public static final JsonDeserialiser<AABB> AXIS_ALIGNED_BB =
            register(AABB.class, new AxisAlignedBBDeserialiser());
    public static final JsonDeserialiser<VoxelShape> VOXEL_SHAPE =
            register(VoxelShape.class, new VoxelShapeDeserialiser());

    public static final JsonDeserialiser<DropCreator.Type<DropContext>> DROP_TYPE =
            register(DropCreator.Type.getGenericClass(), new RegistryEntryDeserialiser<>(DropCreator.Type.REGISTRY));

    public static final JsonDeserialiser<CellKit> CELL_KIT =
            register(CellKit.class, new RegistryEntryDeserialiser<>(CellKit.REGISTRY));
    public static final JsonDeserialiser<LeavesProperties> LEAVES_PROPERTIES =
            register(LeavesProperties.class, new RegistryEntryDeserialiser<>(LeavesProperties.REGISTRY));
    public static final JsonDeserialiser<GrowthLogicKit> GROWTH_LOGIC_KIT =
            register(GrowthLogicKit.class, new RegistryEntryDeserialiser<>(GrowthLogicKit.REGISTRY));
    public static final JsonDeserialiser<GenFeature> GEN_FEATURE =
            register(GenFeature.class, new RegistryEntryDeserialiser<>(GenFeature.REGISTRY));
    public static final JsonDeserialiser<Family> FAMILY =
            register(Family.class, new RegistryEntryDeserialiser<>(Family.REGISTRY));
    public static final JsonDeserialiser<DropCreator> DROP_CREATOR =
            register(DropCreator.class, new RegistryEntryDeserialiser<>(DropCreator.REGISTRY));
    public static final JsonDeserialiser<Species> SPECIES =
            register(Species.class, new RegistryEntryDeserialiser<>(Species.REGISTRY));
    public static final JsonDeserialiser<FeatureCanceller> FEATURE_CANCELLER =
            register(FeatureCanceller.class, new RegistryEntryDeserialiser<>(FeatureCanceller.REGISTRY));
    public static final JsonDeserialiser<SoilProperties> SOIL_PROPERTIES =
            register(SoilProperties.class, new RegistryEntryDeserialiser<>(SoilProperties.REGISTRY));

    public static final JsonDeserialiser<List<SoilProperties>> SOIL_PROPERTIES_LIST =
            register(ListDeserialiser.getListClass(SoilProperties.class), new ListDeserialiser<>(SOIL_PROPERTIES));

    public static final JsonDeserialiser<GenFeatureConfiguration> CONFIGURED_GEN_FEATURE =
            register(GenFeatureConfiguration.class,
                    new ConfiguredDeserialiser<>("Gen Feature", GenFeature.class, GenFeatureConfiguration.TEMPLATES));
    public static final JsonDeserialiser<DropCreatorConfiguration> CONFIGURED_DROP_CREATOR =
            register(DropCreatorConfiguration.class,
                    new ConfiguredDeserialiser<>("Drop Creator", DropCreator.class,
                            DropCreatorConfiguration.TEMPLATES));
    public static final JsonDeserialiser<GrowthLogicKitConfiguration> CONFIGURED_GROWTH_LOGIC_KIT =
            register(GrowthLogicKitConfiguration.class,
                    new ConfiguredDeserialiser<>("Growth Logic Kit", GrowthLogicKit.class,
                            GrowthLogicKitConfiguration.TEMPLATES));

    public static final JsonDeserialiser<Drops> DROPS = register(Drops.class, new DropsDeserialiser());

    public static final JsonDeserialiser<Seed> SEED = register(Seed.class, jsonElement -> ITEM.deserialise(jsonElement)
            .mapIfValid(item -> item instanceof Seed, "Item \"{}\" is not a seed.", item -> (Seed) item));

    public static final JsonDeserialiser<BranchBlock> BRANCH =
            register(BranchBlock.class, jsonElement -> BLOCK.deserialise(jsonElement)
                    .mapIfValid(block -> block instanceof BranchBlock, "Block \"{}\" is not a branch.",
                            block -> (BranchBlock) block));
    public static final JsonDeserialiser<FruitBlock> FRUIT =
            register(FruitBlock.class, jsonElement -> BLOCK.deserialise(jsonElement)
                    .mapIfValid(block -> block instanceof FruitBlock, "Block \"{}\" is not a fruit.",
                            block -> (FruitBlock) block));

    public static final JsonDeserialiser<VinesGenFeature.VineType> VINE_TYPE =
            register(VinesGenFeature.VineType.class, new EnumDeserialiser<>(VinesGenFeature.VineType.class));
    public static final JsonDeserialiser<BiomeDatabase.Operation> OPERATION =
            register(BiomeDatabase.Operation.class, new EnumDeserialiser<>(BiomeDatabase.Operation.class));
    public static final JsonDeserialiser<GenerationStep.Decoration> DECORATION_STAGE =
            register(GenerationStep.Decoration.class, new EnumDeserialiser<>(GenerationStep.Decoration.class));

    public static final JsonDeserialiser<BiomeList> BIOME_LIST = register(BiomeList.class, new BiomeListDeserialiser());
    public static final JsonDeserialiser<BiomePredicate> BIOME_PREDICATE = register(BiomePredicate.class, jsonElement ->
            BIOME_LIST.deserialise(jsonElement).map(biomeList ->
                    biome -> biomeList.stream().anyMatch(currentBiome -> Objects.equals(
                            // TODO: ForgeRegistries.BIOMES does not contain any biomes declared in datapacks. But we don't have a world yet. Anything we can do? -SizableShrimp
                            ForgeRegistries.BIOMES.getKey(currentBiome),
                            ForgeRegistries.BIOMES.getKey(biome)
                    ))
            ));

    public static final JsonDeserialiser<BiomePropertySelectors.SpeciesSelector> SPECIES_SELECTOR = register(
            BiomePropertySelectors.SpeciesSelector.class, new SpeciesSelectorDeserialiser());
    public static final JsonDeserialiser<BiomePropertySelectors.DensitySelector> DENSITY_SELECTOR = register(
            BiomePropertySelectors.DensitySelector.class, new DensitySelectorDeserialiser());
    public static final JsonDeserialiser<BiomePropertySelectors.ChanceSelector> CHANCE_SELECTOR = register(
            BiomePropertySelectors.ChanceSelector.class, new ChanceSelectorDeserialiser());

    public static final JsonDeserialiser<SeedSaplingRecipe> SEED_SAPLING_RECIPE = register(
            SeedSaplingRecipe.class, new SeedSaplingRecipeDeserialiser()
    );

    public static final JsonDeserialiser<Material> MATERIAL =
            register(Material.class, new MaterialDeserialiser());
    public static final JsonDeserialiser<MaterialColor> MATERIAL_COLOR =
            register(MaterialColor.class, new MaterialColorDeserialiser());
    public static final JsonDeserialiser<SoundType> SOUND_TYPE =
            register(SoundType.class, new SoundTypeDeserialiser());

//    private static final Map<String, ToolType> TOOL_TYPES =
//            ReflectionHelper.getPrivateFieldUnchecked(ToolType.class, "VALUES");
//
//    public static final JsonDeserialiser<ToolType> TOOL_TYPE = register(ToolType.class, jsonElement ->
//            STRING.deserialise(jsonElement).map(TOOL_TYPES::get, "Could not get tool type from \"{}\"."));

    public static final JsonDeserialiser<Class<?>> DESERIALISABLE_CLASS = new DeserialisableClassDeserialiser();

    public static final JsonDeserialiser<PropertyDefinition<?>> VARIABLE_DEFINITION =
            register(PropertyDefinition.captureClass(), new PropertyDefinitionDeserialiser());

    /**
     * Registers {@link ForgeRegistryEntryDeserialiser} objects. This should be called after the registries are
     * initiated to avoid giving null to the getters.
     */
    public static void registerForgeEntryGetters() {
        BLOCK = register(Block.class,
                new ForgeRegistryEntryDeserialiser(ForgeRegistries.BLOCKS, "block", Blocks.AIR));
        ITEM = register(Item.class, new ForgeRegistryEntryDeserialiser(ForgeRegistries.ITEMS, "item", Items.AIR));
        // TODO: ForgeRegistries.BIOMES does not contain any biomes declared in datapacks. But we don't have a world yet. Anything we can do? -SizableShrimp
        BIOME = register(Biome.class, new ForgeRegistryEntryDeserialiser(ForgeRegistries.BIOMES, "biome"));
    }

    public static void postRegistryEvent() {
        ModLoader.get().postEvent(new RegistryEvent());
    }

    /**
     * This event is posted for add-ons to register custom Json object getters at the right time.
     */
    public static final class RegistryEvent extends Event implements IModBusEvent {
    }

}
