package com.dtteam.dynamictrees.deserialization;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.deserialization.deserializer.*;
import com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Holds {@link com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer} objects, which can be used to obtain objects from {@link JsonElement} objects.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unused")
public final class JsonDeserializers {

    private static final Map<Class<?>, com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<?>> DESERIALISERS = Maps.newHashMap();

    private static final class NullDeserialiser<O> implements com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<O> {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean deserializeIfValid(JsonElement input, Consumer<Result<O, JsonElement>> consumer) {
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
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<?> NULL = new NullDeserialiser<>();

    /**
     * Gets the {@link com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer} for the given class type.
     *
     * @param type The {@link Class} of the object to get.
     * @param <T>  The type of the object.
     * @return The {@link com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer} for the class, or {@link #NULL} if it wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<T> get(final Class<T> type) {
        return (com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<T>) DESERIALISERS.getOrDefault(type, NULL);
    }

    public static <T> com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<T> getOrThrow(final Class<T> type) throws NoSuchDeserialiserException {
        return getOrThrow(type, "No Json deserialiser found for type \"" + type.getName() + "\".");
    }

    @SuppressWarnings("unchecked")
    public static <T> com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<T> getOrThrow(Class<T> type, String errorMessage)
            throws NoSuchDeserialiserException {
        final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<?> deserialiser = DESERIALISERS.get(type);
        if (deserialiser == null) {
            throw new NoSuchDeserialiserException(errorMessage);
        }
        return ((com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<T>) deserialiser);
    }

    public static Set<Class<?>> getDeserialisableClasses() {
        return DESERIALISERS.keySet();
    }

    /**
     * Registers an {@link com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer} to the registry.
     *
     * @param outputClass  The {@link Class} of the object that will be obtained.
     * @param deserialiser The {@link com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer} to register.
     * @param <T>          The type of the object getter.
     * @return The {@link com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer} given.
     */
    public static <T> com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<T> register(final Class<T> outputClass, final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<T> deserialiser) {
        DESERIALISERS.put(outputClass, deserialiser);
        return deserialiser;
    }

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<JsonElement> JSON_ELEMENT = register(JsonElement.class, input ->
            JsonResult.success(input, input)
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<JsonNull> JSON_NULL = register(JsonNull.class, input ->
            input.isJsonNull() ? JsonResult.success(input, JsonNull.INSTANCE) :
                    JsonResult.failure(input, "Json element was not a json null.")
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<JsonPrimitive> JSON_PRIMITIVE = register(JsonPrimitive.class, input ->
            input.isJsonPrimitive() ? JsonResult.success(input, input.getAsJsonPrimitive()) :
                    JsonResult.failure(input, "Json element was not a json primitive.")
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<JsonObject> JSON_OBJECT = register(JsonObject.class, input ->
            input.isJsonObject() ? JsonResult.success(input, input.getAsJsonObject()) :
                    JsonResult.failure(input, "Json element was not a json object.")
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<JsonArray> JSON_ARRAY = register(JsonArray.class, input ->
            input.isJsonArray() ? JsonResult.success(input, input.getAsJsonArray()) :
                    JsonResult.failure(input, "Json element was not a json array.")
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Boolean> BOOLEAN = register(Boolean.class, input ->
            JSON_PRIMITIVE.deserialise(input).mapIfValid(
                    JsonPrimitive::isBoolean,
                    "Could not get boolean from \"{}\".",
                    JsonPrimitive::getAsBoolean
            )
    );
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Number> NUMBER = register(Number.class, input ->
            JSON_PRIMITIVE.deserialise(input).mapIfValid(
                    JsonPrimitive::isNumber,
                    "Could not get number from \"{}\".",
                    JsonPrimitive::getAsNumber
            )
    );
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<String> STRING = register(String.class, input ->
            JSON_PRIMITIVE.deserialise(input).mapIfValid(
                    JsonPrimitive::isString,
                    "Could not get string from \"{}\".",
                    JsonPrimitive::getAsString
            )
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Byte> BYTE = register(Byte.class, input ->
            NUMBER.deserialise(input).map(Number::byteValue)
    );
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Short> SHORT = register(Short.class, input ->
            NUMBER.deserialise(input).map(Number::shortValue)
    );
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Integer> INTEGER = register(Integer.class, input ->
            NUMBER.deserialise(input).map(Number::intValue)
    );
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Long> LONG = register(Long.class, input ->
            NUMBER.deserialise(input).map(Number::longValue)
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Float> FLOAT = register(Float.class, input ->
            NUMBER.deserialise(input).map(Number::floatValue)
    );
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Double> DOUBLE = register(Double.class, input ->
            NUMBER.deserialise(input).map(Number::doubleValue)
    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<ResourceLocation> RESOURCE_LOCATION =
            register(ResourceLocation.class, ResourceLocationDeserializer.create());

    /**
     * Alternative to {@link #RESOURCE_LOCATION}, defaulting the namespace to {@code dynamictrees}.
     */
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<ResourceLocation> DT_RESOURCE_LOCATION =
            ResourceLocationDeserializer.create(DynamicTrees.MOD_ID);

    public static com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Block> BLOCK;
    public static com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Item> ITEM;

    // TODO: Read json object for quantity and NBT.
    public static com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<ItemStack> ITEM_STACK = register(ItemStack.class,
            input -> ITEM.deserialise(input).map((Result.SimpleMapper<Item, ItemStack>) ItemStack::new));

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<AABB> AABB =
            register(AABB.class, new AxisAlignedBBDeserializer());
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<VoxelShape> VOXEL_SHAPE =
            register(VoxelShape.class, new VoxelShapeDeserializer());

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<CellKit> CELL_KIT =
            register(CellKit.class, new RegistryEntryDeserializer<>(CellKit.REGISTRY));
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<LeavesProperties> LEAVES_PROPERTIES =
            register(LeavesProperties.class, new RegistryEntryDeserializer<>(LeavesProperties.REGISTRY));
//    public static final JsonDeserialiser<GrowthLogicKit> GROWTH_LOGIC_KIT =
//            register(GrowthLogicKit.class, new RegistryEntryDeserialiser<>(GrowthLogicKit.REGISTRY));
//
//    public static final JsonDeserialiser<GenFeature> GEN_FEATURE =
//            register(GenFeature.class, new RegistryEntryDeserialiser<>(GenFeature.REGISTRY));
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Family> FAMILY =
            register(Family.class, new RegistryEntryDeserializer<>(Family.REGISTRY));
//    public static final JsonDeserialiser<Fruit> FRUIT =
//            register(Fruit.class, new RegistryEntryDeserialiser<>(Fruit.REGISTRY));
//    public static final JsonDeserialiser<Pod> POD =
//            register(Pod.class, new RegistryEntryDeserialiser<>(Pod.REGISTRY));
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Species> SPECIES =
            register(Species.class, new RegistryEntryDeserializer<>(Species.REGISTRY));
//    public static final JsonDeserialiser<FeatureCanceller> FEATURE_CANCELLER =
//            register(FeatureCanceller.class, new RegistryEntryDeserialiser<>(FeatureCanceller.REGISTRY));
//    public static final JsonDeserialiser<SoilProperties> SOIL_PROPERTIES =
//            register(SoilProperties.class, new RegistryEntryDeserialiser<>(SoilProperties.REGISTRY));

//    public static final JsonDeserialiser<List<SoilProperties>> SOIL_PROPERTIES_LIST =
//            register(ListDeserialiser.getListClass(SoilProperties.class), new ListDeserialiser<>(SOIL_PROPERTIES));

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<Map<String, ResourceLocation>> RESOURCE_LOCATION_MAP =
            register(MapDeserializer.getMapClass(String.class, ResourceLocation.class), new MapDeserializer<>(STRING, RESOURCE_LOCATION));


//    public static final JsonDeserialiser<GenFeatureConfiguration> CONFIGURED_GEN_FEATURE =
//            register(GenFeatureConfiguration.class,
//                    new ConfiguredDeserialiser<>("Gen Feature", GenFeature.class, GenFeatureConfiguration.TEMPLATES));
//    public static final JsonDeserialiser<GrowthLogicKitConfiguration> CONFIGURED_GROWTH_LOGIC_KIT =
//            register(GrowthLogicKitConfiguration.class,
//                    new ConfiguredDeserialiser<>("Growth Logic Kit", GrowthLogicKit.class,
//                            GrowthLogicKitConfiguration.TEMPLATES));
//
//    public static final JsonDeserialiser<Seed> SEED = register(Seed.class, jsonElement -> ITEM.deserialise(jsonElement)
//            .mapIfValid(item -> item instanceof Seed, "Item \"{}\" is not a seed.", item -> (Seed) item));

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<BranchBlock> BRANCH =
            register(BranchBlock.class, jsonElement -> BLOCK.deserialise(jsonElement)
                    .mapIfValid(block -> block instanceof BranchBlock, "Block \"{}\" is not a branch.",
                            block -> (BranchBlock) block));

//    public static final JsonDeserialiser<VinesGenFeature.VineType> VINE_TYPE =
//            register(VinesGenFeature.VineType.class, new EnumDeserialiser<>(VinesGenFeature.VineType.class));
//    public static final JsonDeserialiser<BiomeDatabase.Operation> OPERATION =
//            register(BiomeDatabase.Operation.class, new EnumDeserialiser<>(BiomeDatabase.Operation.class));
//    public static final JsonDeserialiser<GenerationStep.Decoration> DECORATION_STAGE =
//            register(GenerationStep.Decoration.class, new EnumDeserialiser<>(GenerationStep.Decoration.class));
//
//    public static final JsonDeserialiser<DTBiomeHolderSet> BIOME_LIST = register(DTBiomeHolderSet.class, new BiomeListDeserialiser());
//    public static final JsonDeserialiser<BiomePredicate> BIOME_PREDICATE = register(BiomePredicate.class, jsonElement ->
//            BIOME_LIST.deserialise(jsonElement).map(biomeList ->
//                    biome -> biomeList.stream().anyMatch(currentBiomeHolder -> currentBiomeHolder.equals(biome) || biome.unwrapKey().map(currentBiomeHolder::is).orElse(false))
//            ));
//
//    public static final JsonDeserialiser<BiomePropertySelectors.SpeciesSelector> SPECIES_SELECTOR = register(
//            BiomePropertySelectors.SpeciesSelector.class, new SpeciesSelectorDeserialiser());
//    public static final JsonDeserialiser<BiomePropertySelectors.DensitySelector> DENSITY_SELECTOR = register(
//            BiomePropertySelectors.DensitySelector.class, new DensitySelectorDeserialiser());
//    public static final JsonDeserialiser<BiomePropertySelectors.ChanceSelector> CHANCE_SELECTOR = register(
//            BiomePropertySelectors.ChanceSelector.class, new ChanceSelectorDeserialiser());
//
//    public static final JsonDeserialiser<SeedSaplingRecipe> SEED_SAPLING_RECIPE = register(
//            SeedSaplingRecipe.class, new SeedSaplingRecipeDeserialiser()
//    );

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<MapColor> MAP_COLOR =
            register(MapColor.class, new MapColorDeserializer());
    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<SoundType> SOUND_TYPE =
            register(SoundType.class, new SoundTypeDeserializer());

    public static final com.dtteam.dynamictrees.deserialization.deserializer.JsonDeserializer<BooleanOp> BOOLEAN_FUNCTION = register(
            BooleanOp.class, new BooleanOpDeserializer()
    );

//    private static final Map<String, ToolType> TOOL_TYPES =
//            ReflectionHelper.getPrivateFieldUnchecked(ToolType.class, "VALUES");
//
//    public static final JsonDeserialiser<ToolType> TOOL_TYPE = register(ToolType.class, jsonElement ->
//            STRING.deserialise(jsonElement).map(TOOL_TYPES::get, "Could not get tool type from \"{}\"."));

    public static final JsonDeserializer<Class<?>> DESERIALISABLE_CLASS = new DeserialisableClassDeserializer();

//    public static final JsonDeserialiser<PropertyDefinition<?>> VARIABLE_DEFINITION =
//            register(PropertyDefinition.captureClass(), new PropertyDefinitionDeserialiser());

//    /**
//     * Registers {@link ForgeRegistryEntryDeserialiser} objects. This should be called after the registries are
//     * initiated to avoid giving null to the getters.
//     */
//    public static void registerForgeEntryGetters() {
//        BLOCK = register(Block.class,
//                new ForgeRegistryEntryDeserialiser<>(BuiltInRegistries.BLOCKS, "block", Blocks.AIR));
//        ITEM = register(Item.class, new ForgeRegistryEntryDeserialiser<>(ForgeRegistries.ITEMS, "item", Items.AIR));
//    }
//
//    public static void postRegistryEvent() {
//        ModLoader.get().postEvent(new RegistryEvent());
//    }
//
//    /**
//     * This event is posted for add-ons to register custom Json object getters at the right time.
//     */
//    public static final class RegistryEvent extends Event implements IModBusEvent {
//    }

}
