package com.ferreusveritas.dynamictrees.util.json;

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
import com.ferreusveritas.dynamictrees.worldgen.json.ChanceSelectorGetter;
import com.ferreusveritas.dynamictrees.worldgen.json.DensitySelectorGetter;
import com.ferreusveritas.dynamictrees.worldgen.json.SpeciesSelectorGetter;
import com.google.common.collect.Sets;
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
import java.util.Set;

/**
 * Holds {@link JsonGetter} objects, which can be used to obtain objects from
 * {@link JsonElement} objects.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unused")
public final class JsonGetters {

    private static final Set<JsonObjectGetterHolder<?>> OBJECT_GETTERS = Sets.newHashSet();

    /** Returned by {@link #get(Class)} if an object getter wasn't found. */
    public static final class NullGetter<T> implements JsonGetter<T> {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public FetchResult<T> get(final JsonElement jsonElement) {
            return FetchResult.failure("Could not get Json object getter for json element: " + jsonElement.toString() + ".");
        }
    }

    /**
     * Gets the {@link JsonGetter} for the given class type.
     *
     * @param objectClass The {@link Class} of the object to get.
     * @param <T> The type of the object.
     * @return The {@link JsonGetter} for the class, or {@link NullGetter} if it wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> JsonGetter<T> get(final Class<T> objectClass) {
        return OBJECT_GETTERS.stream().filter(jsonObjectGetterHolder -> jsonObjectGetterHolder.objectClass.equals(objectClass))
                .findFirst().map(jsonObjectGetterHolder -> (JsonGetter<T>) jsonObjectGetterHolder.objectGetter).orElse(new NullGetter<>());
    }

    /**
     * Registers an {@link JsonGetter} to the registry.
     *
     * @param objectClass The {@link Class} of the object that will be obtained.
     * @param objectGetter The {@link JsonGetter} to register.
     * @param <T> The type of the object getter.
     * @return The {@link JsonGetter} given.
     */
    public static <T> JsonGetter<T> register(final Class<T> objectClass, final JsonGetter<T> objectGetter) {
        OBJECT_GETTERS.add(new JsonObjectGetterHolder<>(objectClass, objectGetter));
        return objectGetter;
    }

    /**
     * Holds an {@link JsonGetter} and the relevant {@link Class} of type {@link T}.
     *
     * @param <T> The type the object getter fetches.
     */
    private static final class JsonObjectGetterHolder<T> {
        private final Class<T> objectClass;
        private final JsonGetter<T> objectGetter;

        public JsonObjectGetterHolder(final Class<T> objectClass, final JsonGetter<T> objectGetter) {
            this.objectClass = objectClass;
            this.objectGetter = objectGetter;
        }
    }

    public static final JsonGetter<JsonElement> JSON_ELEMENT = register(JsonElement.class, FetchResult::success);

    public static final JsonGetter<JsonPrimitive> JSON_PRIMITIVE = register(JsonPrimitive.class, jsonElement ->
            jsonElement.isJsonPrimitive() ? FetchResult.success(jsonElement.getAsJsonPrimitive()) :
                    FetchResult.failure("Json element was not a json primitive."));

    public static final JsonGetter<JsonObject> JSON_OBJECT = register(JsonObject.class, jsonElement ->
            jsonElement.isJsonObject() ? FetchResult.success(jsonElement.getAsJsonObject()) :
                    FetchResult.failure("Json element was not a json object."));

    public static final JsonGetter<JsonArray> JSON_ARRAY = register(JsonArray.class, jsonElement ->
            jsonElement.isJsonArray() ? FetchResult.success(jsonElement.getAsJsonArray()) :
                    FetchResult.failure("Json element was not a json array."));

    public static final JsonGetter<Boolean> BOOLEAN = register(Boolean.class, jsonElement -> JSON_PRIMITIVE.get(jsonElement).mapIfValid(JsonPrimitive::isBoolean, "Could not get boolean from '{value}'.", JsonPrimitive::getAsBoolean));
    public static final JsonGetter<Number> NUMBER = register(Number.class, jsonElement -> JSON_PRIMITIVE.get(jsonElement).mapIfValid(JsonPrimitive::isNumber, "Could not get number from '{value}'.", JsonPrimitive::getAsNumber));
    public static final JsonGetter<String> STRING = register(String.class, jsonElement -> JSON_PRIMITIVE.get(jsonElement).mapIfValid(JsonPrimitive::isString, "Could not get string from '{value}'.", JsonPrimitive::getAsString));

    public static final JsonGetter<Byte> BYTE = register(Byte.class, jsonElement -> NUMBER.get(jsonElement).map(Number::byteValue));
    public static final JsonGetter<Short> SHORT = register(Short.class, jsonElement -> NUMBER.get(jsonElement).map(Number::shortValue));
    public static final JsonGetter<Integer> INTEGER = register(Integer.class, jsonElement -> NUMBER.get(jsonElement).map(Number::intValue));
    public static final JsonGetter<Long> LONG = register(Long.class, jsonElement -> NUMBER.get(jsonElement).map(Number::longValue));

    public static final JsonGetter<Float> FLOAT = register(Float.class, jsonElement -> NUMBER.get(jsonElement).map(Number::floatValue));
    public static final JsonGetter<Double> DOUBLE = register(Double.class, jsonElement -> NUMBER.get(jsonElement).map(Number::doubleValue));

    public static final JsonGetter<ResourceLocation> RESOURCE_LOCATION = register(ResourceLocation.class, ResourceLocationGetter.create());
    /** Alternative to {@link #RESOURCE_LOCATION}, defaulting the namespace to {@code dynamictrees}.  */
    public static final JsonGetter<ResourceLocation> DT_RESOURCE_LOCATION = ResourceLocationGetter.create(DynamicTrees.MOD_ID);

    public static JsonGetter<Block> BLOCK;
    public static JsonGetter<Item> ITEM;
    public static JsonGetter<Biome> BIOME;

    // TODO: Read json object for quantity and NBT.
    public static JsonGetter<ItemStack> ITEM_STACK = register(ItemStack.class, jsonElement -> ITEM.get(jsonElement).map(ItemStack::new));

    public static final JsonGetter<AxisAlignedBB> AXIS_ALIGNED_BB = register(AxisAlignedBB.class, new AxisAlignedBBGetter());
    public static final JsonGetter<VoxelShape> VOXEL_SHAPE = register(VoxelShape.class, new VoxelShapeGetter());

    public static final JsonGetter<DropCreator.DropType<DropContext>> DROP_TYPE = register(DropCreator.DropType.getGenericClass(), new RegistryEntryGetter<>(DropCreator.DropType.REGISTRY));

    public static final JsonGetter<CellKit> CELL_KIT = register(CellKit.class, new RegistryEntryGetter<>(CellKit.REGISTRY));
    public static final JsonGetter<LeavesProperties> LEAVES_PROPERTIES = register(LeavesProperties.class, new RegistryEntryGetter<>(LeavesProperties.REGISTRY));
    public static final JsonGetter<GrowthLogicKit> GROWTH_LOGIC_KIT = register(GrowthLogicKit.class, new RegistryEntryGetter<>(GrowthLogicKit.REGISTRY));
    public static final JsonGetter<GenFeature> GEN_FEATURE = register(GenFeature.class, new RegistryEntryGetter<>(GenFeature.REGISTRY));
    public static final JsonGetter<Family> FAMILY = register(Family.class, new RegistryEntryGetter<>(Family.REGISTRY));
    public static final JsonGetter<DropCreator> DROP_CREATOR = register(DropCreator.class, new RegistryEntryGetter<>(DropCreator.REGISTRY));
    public static final JsonGetter<Species> SPECIES = register(Species.class, new RegistryEntryGetter<>(Species.REGISTRY));
    public static final JsonGetter<FeatureCanceller> FEATURE_CANCELLER = register(FeatureCanceller.class, new RegistryEntryGetter<>(FeatureCanceller.REGISTRY));
    public static final JsonGetter<SoilProperties> SOIL_PROPERTIES = register(SoilProperties.class, new RegistryEntryGetter<>(SoilProperties.REGISTRY));

    public static final JsonGetter<List<SoilProperties>> SOIL_PROPERTIES_LIST = register(ListGetter.getListClass(SoilProperties.class), new ListGetter<>(SOIL_PROPERTIES));

    public static final JsonGetter<ConfiguredGenFeature<GenFeature>> CONFIGURED_GEN_FEATURE = register(ConfiguredGenFeature.NULL_CONFIGURED_FEATURE_CLASS, new ConfiguredGetter<>("Gen Feature", GenFeature.class));
    public static final JsonGetter<ConfiguredDropCreator<DropCreator>> CONFIGURED_DROP_CREATOR = register(ConfiguredDropCreator.NULL_CONFIGURED_DROP_CREATOR_CLASS, new ConfiguredGetter<>("Drop Creator", DropCreator.class));

    public static final JsonGetter<Drops> DROPS = register(Drops.class, new DropsGetter());

    public static final JsonGetter<Seed> SEED = register(Seed.class, jsonElement -> ITEM.get(jsonElement).mapIfValid(item -> item instanceof Seed, "Item '{value}' is not a seed.", item -> (Seed) item));

    public static final JsonGetter<BranchBlock> BRANCH = register(BranchBlock.class, jsonElement -> BLOCK.get(jsonElement)
            .mapIfValid(block -> block instanceof BranchBlock, "Block '{value}' is not a branch.", block -> (BranchBlock) block));
    public static final JsonGetter<FruitBlock> FRUIT = register(FruitBlock.class, jsonElement -> BLOCK.get(jsonElement)
            .mapIfValid(block -> block instanceof FruitBlock, "Block '{value}' is not a fruit.", block -> (FruitBlock) block));

    // Random enum getters.
    public static final JsonGetter<VinesGenFeature.VineType> VINE_TYPE = register(VinesGenFeature.VineType.class, new EnumGetter<>(VinesGenFeature.VineType.class));
    public static final JsonGetter<BiomeDatabase.Operation> OPERATION = register(BiomeDatabase.Operation.class, new EnumGetter<>(BiomeDatabase.Operation.class));
    public static final JsonGetter<GenerationStage.Decoration> DECORATION_STAGE = register(GenerationStage.Decoration.class, new EnumGetter<>(GenerationStage.Decoration.class));

    public static final JsonGetter<BiomeList> BIOME_LIST = register(BiomeList.class, new BiomeListGetter());
    public static final JsonGetter<BiomePredicate> BIOME_PREDICATE = register(BiomePredicate.class, jsonElement ->
            BIOME_LIST.get(jsonElement).map(biomeList ->
                    biome -> biomeList.stream().anyMatch(currentBiome -> Objects.equals(
                            currentBiome.getRegistryName(),
                            biome.getRegistryName()
                    ))
            ));

    public static final JsonGetter<BiomePropertySelectors.ISpeciesSelector> SPECIES_SELECTOR = register(
            BiomePropertySelectors.ISpeciesSelector.class, new SpeciesSelectorGetter());
    public static final JsonGetter<BiomePropertySelectors.IDensitySelector> DENSITY_SELECTOR = register(
            BiomePropertySelectors.IDensitySelector.class, new DensitySelectorGetter());
    public static final JsonGetter<BiomePropertySelectors.IChanceSelector> CHANCE_SELECTOR = register(
            BiomePropertySelectors.IChanceSelector.class, new ChanceSelectorGetter());

    public static final JsonGetter<Material> MATERIAL = register(Material.class, new FieldGetter<>(Material.class));
    public static final JsonGetter<MaterialColor> MATERIAL_COLOR = register(MaterialColor.class, new FieldGetter<>(MaterialColor.class));
    public static final JsonGetter<SoundType> SOUND_TYPE = register(SoundType.class, new FieldGetter<>(SoundType.class));

    private static final Map<String, ToolType> TOOL_TYPES = ReflectionHelper.getPrivateFieldUnchecked(ToolType.class, "VALUES");

    public static final JsonGetter<ToolType> TOOL_TYPE = register(ToolType.class, jsonElement ->
            STRING.get(jsonElement).map(TOOL_TYPES::get, "Could not get tool type from '{previous_value}'."));

    /**
     * Registers {@link ForgeRegistryEntryGetter} objects. This should be called after the registries are initiated to avoid
     * giving null to the getters.
     */
    public static void registerForgeEntryGetters() {
        BLOCK = register(Block.class, new ForgeRegistryEntryGetter<>(ForgeRegistries.BLOCKS, "block", Blocks.AIR));
        ITEM = register(Item.class, new ForgeRegistryEntryGetter<>(ForgeRegistries.ITEMS, "item", Items.AIR));
        BIOME = register(Biome.class, new ForgeRegistryEntryGetter<>(ForgeRegistries.BIOMES, "biome"));
    }

    public static void postRegistryEvent() {
        ModLoader.get().postEvent(new RegistryEvent());
    }

    /**
     * This event is posted for add-ons to register custom Json object getters at the right time.
     */
    public static final class RegistryEvent extends Event implements IModBusEvent { }

}
