package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.items.Seed;
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

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Holds {@link IJsonObjectGetter} objects, which can be used to obtain objects from
 * {@link JsonElement} objects.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unused")
public final class JsonObjectGetters {

    private static final Set<JsonObjectGetterHolder<?>> OBJECT_GETTERS = Sets.newHashSet();

    /** Returned by {@link #getObjectGetter(Class)} if an object getter wasn't found. */
    public static final class NullObjectGetter<T> implements IJsonObjectGetter<T> {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public ObjectFetchResult<T> get(final JsonElement jsonElement) {
            return ObjectFetchResult.failure("Could not get Json object getter for json element: " + jsonElement.toString() + ".");
        }
    }

    /**
     * Gets the {@link IJsonObjectGetter} for the given class type.
     *
     * @param objectClass The {@link Class} of the object to get.
     * @param <T> The type of the object.
     * @return The {@link IJsonObjectGetter} for the class, or {@link NullObjectGetter} if it wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> IJsonObjectGetter<T> getObjectGetter (final Class<T> objectClass) {
        return OBJECT_GETTERS.stream().filter(jsonObjectGetterHolder -> jsonObjectGetterHolder.objectClass.equals(objectClass))
                .findFirst().map(jsonObjectGetterHolder -> (IJsonObjectGetter<T>) jsonObjectGetterHolder.objectGetter).orElse(new NullObjectGetter<>());
    }

    /**
     * Registers an {@link IJsonObjectGetter} to the registry.
     *
     * @param objectClass The {@link Class} of the object that will be obtained.
     * @param objectGetter The {@link IJsonObjectGetter} to register.
     * @param <T> The type of the object getter.
     * @return The {@link IJsonObjectGetter} given.
     */
    public static <T> IJsonObjectGetter<T> register(final Class<T> objectClass, final IJsonObjectGetter<T> objectGetter) {
        OBJECT_GETTERS.add(new JsonObjectGetterHolder<>(objectClass, objectGetter));
        return objectGetter;
    }

    /**
     * Holds an {@link IJsonObjectGetter} and the relevant {@link Class} of type {@link T}.
     *
     * @param <T> The type the object getter fetches.
     */
    private static final class JsonObjectGetterHolder<T> {
        private final Class<T> objectClass;
        private final IJsonObjectGetter<T> objectGetter;

        public JsonObjectGetterHolder(final Class<T> objectClass, final IJsonObjectGetter<T> objectGetter) {
            this.objectClass = objectClass;
            this.objectGetter = objectGetter;
        }
    }

    public static final IJsonObjectGetter<JsonElement> JSON_ELEMENT = register(JsonElement.class, ObjectFetchResult::success);

    public static final IJsonObjectGetter<JsonPrimitive> JSON_PRIMITIVE = register(JsonPrimitive.class, jsonElement -> {
        if (!jsonElement.isJsonPrimitive()) {
            return ObjectFetchResult.failure("Json element was not a json primitive.");
        }

        return ObjectFetchResult.success(jsonElement.getAsJsonPrimitive());
    });

    public static final IJsonObjectGetter<JsonObject> JSON_OBJECT = register(JsonObject.class, jsonElement -> {
        if (!jsonElement.isJsonObject())
            return ObjectFetchResult.failure("Json element was not a json object.");

        return ObjectFetchResult.success(jsonElement.getAsJsonObject());
    });

    public static final IJsonObjectGetter<JsonArray> JSON_ARRAY = register(JsonArray.class, jsonElement -> {
        if (!jsonElement.isJsonArray())
            return ObjectFetchResult.failure("Json element was not a json array.");

        return ObjectFetchResult.success(jsonElement.getAsJsonArray());
    });

    public static final IJsonObjectGetter<Boolean> BOOLEAN = register(Boolean.class, jsonElement -> JSON_PRIMITIVE.get(jsonElement).mapIfValid(JsonPrimitive::isBoolean, "Could not get boolean from '{value}'.", JsonPrimitive::getAsBoolean));
    public static final IJsonObjectGetter<Number> NUMBER = register(Number.class, jsonElement -> JSON_PRIMITIVE.get(jsonElement).mapIfValid(JsonPrimitive::isNumber, "Could not get number from '{value}'.", JsonPrimitive::getAsNumber));
    public static final IJsonObjectGetter<String> STRING = register(String.class, jsonElement -> JSON_PRIMITIVE.get(jsonElement).mapIfValid(JsonPrimitive::isString, "Could not get string from '{value}'.", JsonPrimitive::getAsString));

    public static final IJsonObjectGetter<Byte> BYTE = register(Byte.class, jsonElement -> NUMBER.get(jsonElement).map(Number::byteValue));
    public static final IJsonObjectGetter<Short> SHORT = register(Short.class, jsonElement -> NUMBER.get(jsonElement).map(Number::shortValue));
    public static final IJsonObjectGetter<Integer> INTEGER = register(Integer.class, jsonElement -> NUMBER.get(jsonElement).map(Number::intValue));
    public static final IJsonObjectGetter<Long> LONG = register(Long.class, jsonElement -> NUMBER.get(jsonElement).map(Number::longValue));

    public static final IJsonObjectGetter<Float> FLOAT = register(Float.class, jsonElement -> NUMBER.get(jsonElement).map(Number::floatValue));
    public static final IJsonObjectGetter<Double> DOUBLE = register(Double.class, jsonElement -> NUMBER.get(jsonElement).map(Number::doubleValue));

    public static final IJsonObjectGetter<ResourceLocation> RESOURCE_LOCATION = register(ResourceLocation.class, ResourceLocationGetter.create());

    public static IJsonObjectGetter<Block> BLOCK;
    public static IJsonObjectGetter<Item> ITEM;
    public static IJsonObjectGetter<Biome> BIOME;

    public static final IJsonObjectGetter<AxisAlignedBB> AXIS_ALIGNED_BB = register(AxisAlignedBB.class, new AxisAlignedBBGetter());
    public static final IJsonObjectGetter<VoxelShape> VOXEL_SHAPE = register(VoxelShape.class, new VoxelShapeGetter());

    public static final IJsonObjectGetter<CellKit> CELL_KIT = register(CellKit.class, new RegistryEntryGetter<>(CellKit.REGISTRY));
    public static final IJsonObjectGetter<LeavesProperties> LEAVES_PROPERTIES = register(LeavesProperties.class, new RegistryEntryGetter<>(LeavesProperties.REGISTRY));
    public static final IJsonObjectGetter<GrowthLogicKit> GROWTH_LOGIC_KIT = register(GrowthLogicKit.class, new RegistryEntryGetter<>(GrowthLogicKit.REGISTRY));
    public static final IJsonObjectGetter<GenFeature> GEN_FEATURE = register(GenFeature.class, new RegistryEntryGetter<>(GenFeature.REGISTRY));
    public static final IJsonObjectGetter<Family> FAMILY = register(Family.class, new RegistryEntryGetter<>(Family.REGISTRY));
    public static final IJsonObjectGetter<Species> SPECIES = register(Species.class, new RegistryEntryGetter<>(Species.REGISTRY));
    public static final IJsonObjectGetter<FeatureCanceller> FEATURE_CANCELLER = register(FeatureCanceller.class, new RegistryEntryGetter<>(FeatureCanceller.REGISTRY));

    public static final IJsonObjectGetter<ConfiguredGenFeature<GenFeature>> CONFIGURED_GEN_FEATURE = register(ConfiguredGenFeature.NULL_CONFIGURED_FEATURE_CLASS, new ConfiguredGenFeatureGetter());

    public static final IJsonObjectGetter<Seed> SEED = register(Seed.class, jsonElement -> ITEM.get(jsonElement).mapIfValid(item -> item instanceof Seed, "Item '{value}' is not a seed.", item -> (Seed) item));

    public static final IJsonObjectGetter<BranchBlock> BRANCH = register(BranchBlock.class, jsonElement -> BLOCK.get(jsonElement)
            .mapIfValid(block -> block instanceof BranchBlock, "Block '{value}' is not a branch.", block -> (BranchBlock) block));
    public static final IJsonObjectGetter<FruitBlock> FRUIT = register(FruitBlock.class, jsonElement -> BLOCK.get(jsonElement)
            .mapIfValid(block -> block instanceof FruitBlock, "Block '{value}' is not a fruit.", block -> (FruitBlock) block));

    // Random enum getters.
    public static final IJsonObjectGetter<VinesGenFeature.VineType> VINE_TYPE = register(VinesGenFeature.VineType.class, new EnumGetter<>(VinesGenFeature.VineType.class));
    public static final IJsonObjectGetter<BiomeDatabase.Operation> OPERATION = register(BiomeDatabase.Operation.class, new EnumGetter<>(BiomeDatabase.Operation.class));
    public static final IJsonObjectGetter<GenerationStage.Decoration> DECORATION_STAGE = register(GenerationStage.Decoration.class, new EnumGetter<>(GenerationStage.Decoration.class));

    public static final IJsonObjectGetter<BiomeList> BIOME_LIST = register(BiomeList.class, new BiomeListGetter());
    public static final IJsonObjectGetter<BiomePredicate> BIOME_PREDICATE = register(BiomePredicate.class, jsonElement -> {
        final ObjectFetchResult<BiomeList> biomeListFetchResult = BIOME_LIST.get(jsonElement);

        if (!biomeListFetchResult.wasSuccessful())
            return ObjectFetchResult.failureFromOther(biomeListFetchResult);

        return ObjectFetchResult.success(biome -> biomeListFetchResult.getValue().stream().anyMatch(currentBiome -> Objects.equals(currentBiome.getRegistryName(), biome.getRegistryName())));
    });

    public static final IJsonObjectGetter<BiomePropertySelectors.ISpeciesSelector> SPECIES_SELECTOR = register(
            BiomePropertySelectors.ISpeciesSelector.class, new SpeciesSelectorGetter());
    public static final IJsonObjectGetter<BiomePropertySelectors.IDensitySelector> DENSITY_SELECTOR = register(
            BiomePropertySelectors.IDensitySelector.class, new DensitySelectorGetter());
    public static final IJsonObjectGetter<BiomePropertySelectors.IChanceSelector> CHANCE_SELECTOR = register(
            BiomePropertySelectors.IChanceSelector.class, new ChanceSelectorGetter());

    public static final IJsonObjectGetter<Material> MATERIAL = register(Material.class, new FieldGetter<>(Material.class));
    public static final IJsonObjectGetter<MaterialColor> MATERIAL_COLOR = register(MaterialColor.class, new FieldGetter<>(MaterialColor.class));
    public static final IJsonObjectGetter<SoundType> SOUND_TYPE = register(SoundType.class, new FieldGetter<>(SoundType.class));

    private static final Map<String, ToolType> TOOL_TYPES = ReflectionHelper.getPrivateFieldUnchecked(ToolType.class, "VALUES");

    public static final IJsonObjectGetter<ToolType> TOOL_TYPE = register(ToolType.class, jsonElement ->
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
