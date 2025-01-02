package com.dtteam.dynamictrees.deserialization;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.api.configuration.PropertyDefinition;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer;
import com.dtteam.dynamictrees.deserialization.deserializer.*;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.SeedSaplingRecipe;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKitConfiguration;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Holds {@link JsonDeserializer} objects, which can be used to obtain objects from {@link JsonElement} objects.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unused")
public final class JsonDeserializers {

    private static final Map<Class<?>, JsonDeserializer<?>> DESERIALIZERS = Maps.newHashMap();

    private static final class NullDeserializer<O> implements JsonDeserializer<O> {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean deserializeIfValid(JsonElement input, Consumer<Result<O, JsonElement>> consumer) {
            return false;
        }

        @Override
        public Result<O, JsonElement> deserialize(JsonElement input) {
            return JsonResult.failure(input, "Could not get Json deserializer for json element: " + input + ".");
        }
    }

    /**
     * Returned by {@link #get(Class)} if an object getter wasn't found.
     */
    public static final JsonDeserializer<?> NULL = new NullDeserializer<>();

    /**
     * Gets the {@link JsonDeserializer} for the given class type.
     *
     * @param type The {@link Class} of the object to get.
     * @param <T>  The type of the object.
     * @return The {@link JsonDeserializer} for the class, or {@link #NULL} if it wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonDeserializer<T> get(final Class<T> type) {
        return (JsonDeserializer<T>) DESERIALIZERS.getOrDefault(type, NULL);
    }

    public static <T> JsonDeserializer<T> getOrThrow(final Class<T> type) throws NoSuchDeserializerException {
        return getOrThrow(type, "No Json deserializer found for type \"" + type.getName() + "\".");
    }

    @SuppressWarnings("unchecked")
    public static <T> JsonDeserializer<T> getOrThrow(Class<T> type, String errorMessage)
            throws NoSuchDeserializerException {
        final JsonDeserializer<?> deserializer = DESERIALIZERS.get(type);
        if (deserializer == null) {
            throw new NoSuchDeserializerException(errorMessage);
        }
        return ((JsonDeserializer<T>) deserializer);
    }

    public static Set<Class<?>> getDeserializableClasses() {
        return DESERIALIZERS.keySet();
    }

    /**
     * Registers an {@link JsonDeserializer} to the registry.
     *
     * @param outputClass  The {@link Class} of the object that will be obtained.
     * @param deserializer The {@link JsonDeserializer} to register.
     * @param <T>          The type of the object getter.
     * @return The {@link JsonDeserializer} given.
     */
    public static <T> JsonDeserializer<T> register(final Class<T> outputClass, final JsonDeserializer<T> deserializer) {
        DESERIALIZERS.put(outputClass, deserializer);
        return deserializer;
    }

    public static final JsonDeserializer<JsonElement> JSON_ELEMENT = register(JsonElement.class, input ->
            JsonResult.success(input, input)
    );

    public static final JsonDeserializer<JsonNull> JSON_NULL = register(JsonNull.class, input ->
            input.isJsonNull() ? JsonResult.success(input, JsonNull.INSTANCE) :
                    JsonResult.failure(input, "Json element was not a json null.")
    );

    public static final JsonDeserializer<JsonPrimitive> JSON_PRIMITIVE = register(JsonPrimitive.class, input ->
            input.isJsonPrimitive() ? JsonResult.success(input, input.getAsJsonPrimitive()) :
                    JsonResult.failure(input, "Json element was not a json primitive.")
    );

    public static final JsonDeserializer<JsonObject> JSON_OBJECT = register(JsonObject.class, input ->
            input.isJsonObject() ? JsonResult.success(input, input.getAsJsonObject()) :
                    JsonResult.failure(input, "Json element was not a json object.")
    );

    public static final JsonDeserializer<JsonArray> JSON_ARRAY = register(JsonArray.class, input ->
            input.isJsonArray() ? JsonResult.success(input, input.getAsJsonArray()) :
                    JsonResult.failure(input, "Json element was not a json array.")
    );

    public static final JsonDeserializer<Boolean> BOOLEAN = register(Boolean.class, input ->
            JSON_PRIMITIVE.deserialize(input).mapIfValid(
                    JsonPrimitive::isBoolean,
                    "Could not get boolean from \"{}\".",
                    JsonPrimitive::getAsBoolean
            )
    );
    public static final JsonDeserializer<Number> NUMBER = register(Number.class, input ->
            JSON_PRIMITIVE.deserialize(input).mapIfValid(
                    JsonPrimitive::isNumber,
                    "Could not get number from \"{}\".",
                    JsonPrimitive::getAsNumber
            )
    );
    public static final JsonDeserializer<String> STRING = register(String.class, input ->
            JSON_PRIMITIVE.deserialize(input).mapIfValid(
                    JsonPrimitive::isString,
                    "Could not get string from \"{}\".",
                    JsonPrimitive::getAsString
            )
    );

    public static final JsonDeserializer<Byte> BYTE = register(Byte.class, input ->
            NUMBER.deserialize(input).map(Number::byteValue)
    );
    public static final JsonDeserializer<Short> SHORT = register(Short.class, input ->
            NUMBER.deserialize(input).map(Number::shortValue)
    );
    public static final JsonDeserializer<Integer> INTEGER = register(Integer.class, input ->
            NUMBER.deserialize(input).map(Number::intValue)
    );
    public static final JsonDeserializer<Long> LONG = register(Long.class, input ->
            NUMBER.deserialize(input).map(Number::longValue)
    );

    public static final JsonDeserializer<Float> FLOAT = register(Float.class, input ->
            NUMBER.deserialize(input).map(Number::floatValue)
    );
    public static final JsonDeserializer<Double> DOUBLE = register(Double.class, input ->
            NUMBER.deserialize(input).map(Number::doubleValue)
    );

    public static final JsonDeserializer<ResourceLocation> RESOURCE_LOCATION =
            register(ResourceLocation.class, ResourceLocationDeserializer.create());

    /**
     * Alternative to {@link #RESOURCE_LOCATION}, defaulting the namespace to {@code dynamictrees}.
     */
    public static final JsonDeserializer<ResourceLocation> DT_RESOURCE_LOCATION =
            ResourceLocationDeserializer.create(DynamicTrees.MOD_ID);

    public static JsonDeserializer<Block> BLOCK;
    public static JsonDeserializer<Item> ITEM;

    // TODO: Read json object for quantity and NBT.
    public static JsonDeserializer<ItemStack> ITEM_STACK = register(ItemStack.class,
            input -> ITEM.deserialize(input).map((Result.SimpleMapper<Item, ItemStack>) ItemStack::new));

    public static final JsonDeserializer<AABB> AABB =
            register(AABB.class, new AxisAlignedBBDeserializer());
    public static final JsonDeserializer<VoxelShape> VOXEL_SHAPE =
            register(VoxelShape.class, new VoxelShapeDeserializer());

    public static final JsonDeserializer<CellKit> CELL_KIT =
            register(CellKit.class, new RegistryEntryDeserializer<>(CellKit.REGISTRY));
    public static final JsonDeserializer<LeavesProperties> LEAVES_PROPERTIES =
            register(LeavesProperties.class, new RegistryEntryDeserializer<>(LeavesProperties.REGISTRY));
    public static final JsonDeserializer<GrowthLogicKit> GROWTH_LOGIC_KIT =
            register(GrowthLogicKit.class, new RegistryEntryDeserializer<>(GrowthLogicKit.REGISTRY));

    public static final JsonDeserializer<GenFeature> GEN_FEATURE =
            register(GenFeature.class, new RegistryEntryDeserializer<>(GenFeature.REGISTRY));
    public static final JsonDeserializer<Family> FAMILY =
            register(Family.class, new RegistryEntryDeserializer<>(Family.REGISTRY));
    public static final JsonDeserializer<Fruit> FRUIT =
            register(Fruit.class, new RegistryEntryDeserializer<>(Fruit.REGISTRY));
    public static final JsonDeserializer<Pod> POD =
            register(Pod.class, new RegistryEntryDeserializer<>(Pod.REGISTRY));
    public static final JsonDeserializer<Species> SPECIES =
            register(Species.class, new RegistryEntryDeserializer<>(Species.REGISTRY));
    public static final JsonDeserializer<SoilProperties> SOIL_PROPERTIES =
            register(SoilProperties.class, new RegistryEntryDeserializer<>(SoilProperties.REGISTRY));
    public static final JsonDeserializer<List<SoilProperties>> SOIL_PROPERTIES_LIST =
            register(ListDeserializer.getListClass(SoilProperties.class), new ListDeserializer<>(SOIL_PROPERTIES));

//    public static final JsonDeserializer<FeatureCanceller> FEATURE_CANCELLER =
//            register(FeatureCanceller.class, new RegistryEntryDeserializer<>(FeatureCanceller.REGISTRY));

    public static final JsonDeserializer<Map<String, ResourceLocation>> RESOURCE_LOCATION_MAP =
            register(MapDeserializer.getMapClass(String.class, ResourceLocation.class), new MapDeserializer<>(STRING, RESOURCE_LOCATION));


    public static final JsonDeserializer<GenFeatureConfiguration> CONFIGURED_GEN_FEATURE =
            register(GenFeatureConfiguration.class,
                    new ConfiguredDeserializer<>("Gen Feature", GenFeature.class, GenFeatureConfiguration.TEMPLATES));
    public static final JsonDeserializer<GrowthLogicKitConfiguration> CONFIGURED_GROWTH_LOGIC_KIT =
            register(GrowthLogicKitConfiguration.class,
                    new ConfiguredDeserializer<>("Growth Logic Kit", GrowthLogicKit.class,
                            GrowthLogicKitConfiguration.TEMPLATES));

    public static final JsonDeserializer<Seed> SEED = register(Seed.class, jsonElement -> ITEM.deserialize(jsonElement)
            .mapIfValid(item -> item instanceof Seed, "Item \"{}\" is not a seed.", item -> (Seed) item));

    public static final JsonDeserializer<BranchBlock> BRANCH =
            register(BranchBlock.class, jsonElement -> BLOCK.deserialize(jsonElement)
                    .mapIfValid(block -> block instanceof BranchBlock, "Block \"{}\" is not a branch.",
                            block -> (BranchBlock) block));

//    public static final JsonDeserializer<VinesGenFeature.VineType> VINE_TYPE =
//            register(VinesGenFeature.VineType.class, new EnumDeserializer<>(VinesGenFeature.VineType.class));
//    public static final JsonDeserializer<BiomeDatabase.Operation> OPERATION =
//            register(BiomeDatabase.Operation.class, new EnumDeserializer<>(BiomeDatabase.Operation.class));
    public static final JsonDeserializer<GenerationStep.Decoration> DECORATION_STAGE =
            register(GenerationStep.Decoration.class, new EnumDeserializer<>(GenerationStep.Decoration.class));

//    public static final JsonDeserializer<DTBiomeHolderSet> BIOME_LIST = register(DTBiomeHolderSet.class, new BiomeListDeserializer());
//    public static final JsonDeserializer<BiomePredicate> BIOME_PREDICATE = register(BiomePredicate.class, jsonElement ->
//            BIOME_LIST.deserialize(jsonElement).map(biomeList ->
//                    biome -> biomeList.stream().anyMatch(currentBiomeHolder -> currentBiomeHolder.equals(biome) || biome.unwrapKey().map(currentBiomeHolder::is).orElse(false))
//            ));
//
//    public static final JsonDeserializer<BiomePropertySelectors.SpeciesSelector> SPECIES_SELECTOR = register(
//            BiomePropertySelectors.SpeciesSelector.class, new SpeciesSelectorDeserializer());
//    public static final JsonDeserializer<BiomePropertySelectors.DensitySelector> DENSITY_SELECTOR = register(
//            BiomePropertySelectors.DensitySelector.class, new DensitySelectorDeserializer());
//    public static final JsonDeserializer<BiomePropertySelectors.ChanceSelector> CHANCE_SELECTOR = register(
//            BiomePropertySelectors.ChanceSelector.class, new ChanceSelectorDeserializer());

    public static final JsonDeserializer<SeedSaplingRecipe> SEED_SAPLING_RECIPE = register(
            SeedSaplingRecipe.class, new SeedSaplingRecipeDeserializer()
    );

    public static final JsonDeserializer<MapColor> MAP_COLOR =
            register(MapColor.class, new MapColorDeserializer());
    public static final JsonDeserializer<SoundType> SOUND_TYPE =
            register(SoundType.class, new SoundTypeDeserializer());

    public static final JsonDeserializer<BooleanOp> BOOLEAN_FUNCTION = register(
            BooleanOp.class, new BooleanOpDeserializer()
    );

    public static final JsonDeserializer<Class<?>> DESERIALIZABLE_CLASS = new DeserializableClassDeserializer();

    public static final JsonDeserializer<PropertyDefinition<?>> VARIABLE_DEFINITION =
            register(PropertyDefinition.captureClass(), new PropertyDefinitionDeserializer());

    /**
     * Registers {@link BuiltInRegistryEntryDeserializer} objects. This should be called after the registries are
     * initiated to avoid giving null to the getters.
     */
    public static void registerRegistryEntryGetters() {
        BLOCK = register(Block.class,
                new BuiltInRegistryEntryDeserializer<>(BuiltInRegistries.BLOCK, "block", Blocks.AIR));
        ITEM = register(Item.class, new BuiltInRegistryEntryDeserializer<>(BuiltInRegistries.ITEM, "item", Items.AIR));
    }

    public static void postRegistryEvent() {
        Services.EVENT.postJsonDeserializerRegistryEvent();
    }


}
