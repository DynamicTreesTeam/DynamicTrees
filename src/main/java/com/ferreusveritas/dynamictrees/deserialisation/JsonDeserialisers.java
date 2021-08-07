package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.dropcreators.ConfiguredDropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.Drops;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.VinesGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.BiomePredicate;
import com.ferreusveritas.dynamictrees.util.ReflectionHelper;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import com.ferreusveritas.dynamictrees.worldgen.deserialisation.ChanceSelectorDeserialiser;
import com.ferreusveritas.dynamictrees.worldgen.deserialisation.DensitySelectorDeserialiser;
import com.ferreusveritas.dynamictrees.worldgen.deserialisation.SpeciesSelectorDeserialiser;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        public boolean deserialiseIfValid(JsonElement input, Consumer<DeserialisationResult<O>> consumer) {
            return false;
        }

        @Override
        public DeserialisationResult<O> deserialise(JsonElement input) {
            return DeserialisationResult.failure("Could not get Json deserialiser for json element: " + input + ".");
        }
    }

    /**
     * Returned by {@link #get(Class)} if an object getter wasn't found.
     */
    public static final JsonDeserialiser<?> NULL = new NullDeserialiser<>();

    /**
     * Gets the {@link JsonDeserialiser} for the given class type.
     *
     * @param objectClass The {@link Class} of the object to get.
     * @param <T>         The type of the object.
     * @return The {@link JsonDeserialiser} for the class, or {@link #NULL} if it wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonDeserialiser<T> get(final Class<T> objectClass) {
        return (JsonDeserialiser<T>) DESERIALISERS.getOrDefault(objectClass, NULL);
    }

    /**
     * Registers an {@link JsonDeserialiser} to the registry.
     *
     * @param outputClass  The {@link Class} of the object that will be obtained.
     * @param deserialiser The {@link JsonDeserialiser} to register.
     * @param <T>          The type of the object getter.
     * @return The {@link JsonDeserialiser} given.
     */
    public static <T> JsonDeserialiser<T> register(final Class<T> outputClass, final JsonDeserialiser<T> deserialiser) {
        DESERIALISERS.put(outputClass, deserialiser);
        return deserialiser;
    }

    public static final JsonDeserialiser<JsonElement> JSON_ELEMENT = register(JsonElement.class, DeserialisationResult::success);

    public static final JsonDeserialiser<JsonPrimitive> JSON_PRIMITIVE = register(JsonPrimitive.class, jsonElement ->
            jsonElement.isJsonPrimitive() ? DeserialisationResult.success(jsonElement.getAsJsonPrimitive()) :
                    DeserialisationResult.failure("Json element was not a json primitive."));

    public static final JsonDeserialiser<JsonObject> JSON_OBJECT = register(JsonObject.class, jsonElement ->
            jsonElement.isJsonObject() ? DeserialisationResult.success(jsonElement.getAsJsonObject()) :
                    DeserialisationResult.failure("Json element was not a json object."));

    public static final JsonDeserialiser<JsonArray> JSON_ARRAY = register(JsonArray.class, jsonElement ->
            jsonElement.isJsonArray() ? DeserialisationResult.success(jsonElement.getAsJsonArray()) :
                    DeserialisationResult.failure("Json element was not a json array."));

    public static final JsonDeserialiser<Boolean> BOOLEAN = register(Boolean.class, jsonElement -> JSON_PRIMITIVE.deserialise(jsonElement).mapIfValid(JsonPrimitive::isBoolean, "Could not get boolean from '{value}'.", JsonPrimitive::getAsBoolean));
    public static final JsonDeserialiser<Number> NUMBER = register(Number.class, jsonElement -> JSON_PRIMITIVE.deserialise(jsonElement).mapIfValid(JsonPrimitive::isNumber, "Could not get number from '{value}'.", JsonPrimitive::getAsNumber));
    public static final JsonDeserialiser<String> STRING = register(String.class, jsonElement -> JSON_PRIMITIVE.deserialise(jsonElement).mapIfValid(JsonPrimitive::isString, "Could not get string from '{value}'.", JsonPrimitive::getAsString));

    public static final JsonDeserialiser<Byte> BYTE = register(Byte.class, jsonElement -> NUMBER.deserialise(jsonElement).map(Number::byteValue));
    public static final JsonDeserialiser<Short> SHORT = register(Short.class, jsonElement -> NUMBER.deserialise(jsonElement).map(Number::shortValue));
    public static final JsonDeserialiser<Integer> INTEGER = register(Integer.class, jsonElement -> NUMBER.deserialise(jsonElement).map(Number::intValue));
    public static final JsonDeserialiser<Long> LONG = register(Long.class, jsonElement -> NUMBER.deserialise(jsonElement).map(Number::longValue));

    public static final JsonDeserialiser<Float> FLOAT = register(Float.class, jsonElement -> NUMBER.deserialise(jsonElement).map(Number::floatValue));
    public static final JsonDeserialiser<Double> DOUBLE = register(Double.class, jsonElement -> NUMBER.deserialise(jsonElement).map(Number::doubleValue));

    public static final JsonDeserialiser<ResourceLocation> RESOURCE_LOCATION = register(ResourceLocation.class, ResourceLocationDeserialiser.create());
    /**
     * Alternative to {@link #RESOURCE_LOCATION}, defaulting the namespace to {@code dynamictrees}.
     */
    public static final JsonDeserialiser<ResourceLocation> DT_RESOURCE_LOCATION = ResourceLocationDeserialiser.create(DynamicTrees.MOD_ID);

    public static JsonDeserialiser<Block> BLOCK;
    public static JsonDeserialiser<Item> ITEM;
    public static JsonDeserialiser<Biome> BIOME;

    // TODO: Read json object for quantity and NBT.
    public static JsonDeserialiser<ItemStack> ITEM_STACK = register(ItemStack.class, jsonElement -> ITEM.deserialise(jsonElement).map(ItemStack::new));

    public static final JsonDeserialiser<AxisAlignedBB> AXIS_ALIGNED_BB = register(AxisAlignedBB.class, new AxisAlignedBBDeserialiser());
    public static final JsonDeserialiser<VoxelShape> VOXEL_SHAPE = register(VoxelShape.class, new VoxelShapeDeserialiser());

    public static final JsonDeserialiser<DropCreator.DropType<DropContext>> DROP_TYPE = register(DropCreator.DropType.getGenericClass(), new RegistryEntryDeserialiser<>(DropCreator.DropType.REGISTRY));

    public static final JsonDeserialiser<CellKit> CELL_KIT = register(CellKit.class, new RegistryEntryDeserialiser<>(CellKit.REGISTRY));
    public static final JsonDeserialiser<LeavesProperties> LEAVES_PROPERTIES = register(LeavesProperties.class, new RegistryEntryDeserialiser<>(LeavesProperties.REGISTRY));
    public static final JsonDeserialiser<GrowthLogicKit> GROWTH_LOGIC_KIT = register(GrowthLogicKit.class, new RegistryEntryDeserialiser<>(GrowthLogicKit.REGISTRY));
    public static final JsonDeserialiser<GenFeature> GEN_FEATURE = register(GenFeature.class, new RegistryEntryDeserialiser<>(GenFeature.REGISTRY));
    public static final JsonDeserialiser<Family> FAMILY = register(Family.class, new RegistryEntryDeserialiser<>(Family.REGISTRY));
    public static final JsonDeserialiser<DropCreator> DROP_CREATOR = register(DropCreator.class, new RegistryEntryDeserialiser<>(DropCreator.REGISTRY));
    public static final JsonDeserialiser<Species> SPECIES = register(Species.class, new RegistryEntryDeserialiser<>(Species.REGISTRY));
    public static final JsonDeserialiser<FeatureCanceller> FEATURE_CANCELLER = register(FeatureCanceller.class, new RegistryEntryDeserialiser<>(FeatureCanceller.REGISTRY));
    public static final JsonDeserialiser<SoilProperties> SOIL_PROPERTIES = register(SoilProperties.class, new RegistryEntryDeserialiser<>(SoilProperties.REGISTRY));

    public static final JsonDeserialiser<List<SoilProperties>> SOIL_PROPERTIES_LIST = register(ListDeserialiser.getListClass(SoilProperties.class), new ListDeserialiser<>(SOIL_PROPERTIES));

    public static final JsonDeserialiser<ConfiguredGenFeature<GenFeature>> CONFIGURED_GEN_FEATURE = register(ConfiguredGenFeature.NULL_CONFIGURED_FEATURE_CLASS, new ConfiguredDeserialiser<>("Gen Feature", GenFeature.class));
    public static final JsonDeserialiser<ConfiguredDropCreator<DropCreator>> CONFIGURED_DROP_CREATOR = register(ConfiguredDropCreator.NULL_CONFIGURED_DROP_CREATOR_CLASS, new ConfiguredDeserialiser<>("Drop Creator", DropCreator.class));

    public static final JsonDeserialiser<Drops> DROPS = register(Drops.class, new DropsDeserialiser());

    public static final JsonDeserialiser<Seed> SEED = register(Seed.class, jsonElement -> ITEM.deserialise(jsonElement).mapIfValid(item -> item instanceof Seed, "Item '{value}' is not a seed.", item -> (Seed) item));

    public static final JsonDeserialiser<BranchBlock> BRANCH = register(BranchBlock.class, jsonElement -> BLOCK.deserialise(jsonElement)
            .mapIfValid(block -> block instanceof BranchBlock, "Block '{value}' is not a branch.", block -> (BranchBlock) block));
    public static final JsonDeserialiser<FruitBlock> FRUIT = register(FruitBlock.class, jsonElement -> BLOCK.deserialise(jsonElement)
            .mapIfValid(block -> block instanceof FruitBlock, "Block '{value}' is not a fruit.", block -> (FruitBlock) block));

    // Random enum getters.
    public static final JsonDeserialiser<VinesGenFeature.VineType> VINE_TYPE = register(VinesGenFeature.VineType.class, new EnumDeserialiser<>(VinesGenFeature.VineType.class));
    public static final JsonDeserialiser<BiomeDatabase.Operation> OPERATION = register(BiomeDatabase.Operation.class, new EnumDeserialiser<>(BiomeDatabase.Operation.class));
    public static final JsonDeserialiser<GenerationStage.Decoration> DECORATION_STAGE = register(GenerationStage.Decoration.class, new EnumDeserialiser<>(GenerationStage.Decoration.class));

    public static final JsonDeserialiser<BiomeList> BIOME_LIST = register(BiomeList.class, new BiomeListDeserialiser());
    public static final JsonDeserialiser<BiomePredicate> BIOME_PREDICATE = register(BiomePredicate.class, jsonElement ->
            BIOME_LIST.deserialise(jsonElement).map(biomeList ->
                    biome -> biomeList.stream().anyMatch(currentBiome -> Objects.equals(
                            currentBiome.getRegistryName(),
                            biome.getRegistryName()
                    ))
            ));

    public static final JsonDeserialiser<BiomePropertySelectors.ISpeciesSelector> SPECIES_SELECTOR = register(
            BiomePropertySelectors.ISpeciesSelector.class, new SpeciesSelectorDeserialiser());
    public static final JsonDeserialiser<BiomePropertySelectors.IDensitySelector> DENSITY_SELECTOR = register(
            BiomePropertySelectors.IDensitySelector.class, new DensitySelectorDeserialiser());
    public static final JsonDeserialiser<BiomePropertySelectors.IChanceSelector> CHANCE_SELECTOR = register(
            BiomePropertySelectors.IChanceSelector.class, new ChanceSelectorDeserialiser());

    public static final JsonDeserialiser<Material> MATERIAL = register(Material.class, new StaticFieldDeserialiser<>(Material.class));
    public static final JsonDeserialiser<MaterialColor> MATERIAL_COLOR = register(MaterialColor.class, new StaticFieldDeserialiser<>(MaterialColor.class));
    public static final JsonDeserialiser<SoundType> SOUND_TYPE = register(SoundType.class, new StaticFieldDeserialiser<>(SoundType.class));

    private static final Map<String, ToolType> TOOL_TYPES = ReflectionHelper.getPrivateFieldUnchecked(ToolType.class, "VALUES");

    public static final JsonDeserialiser<ToolType> TOOL_TYPE = register(ToolType.class, jsonElement ->
            STRING.deserialise(jsonElement).map(TOOL_TYPES::get, "Could not get tool type from '{previous_value}'."));

    /**
     * Registers {@link ForgeRegistryEntryDeserialiser} objects. This should be called after the registries are
     * initiated to avoid giving null to the getters.
     */
    public static void registerForgeEntryGetters() {
        BLOCK = register(Block.class, new ForgeRegistryEntryDeserialiser<>(ForgeRegistries.BLOCKS, "block", Blocks.AIR));
        ITEM = register(Item.class, new ForgeRegistryEntryDeserialiser<>(ForgeRegistries.ITEMS, "item", Items.AIR));
        BIOME = register(Biome.class, new ForgeRegistryEntryDeserialiser<>(ForgeRegistries.BIOMES, "biome"));
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
