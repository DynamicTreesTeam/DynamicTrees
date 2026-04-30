package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpeciesBlockEntity;
import com.dtteam.dynamictrees.command.HexColorArgument;
import com.dtteam.dynamictrees.data.components.BranchDestructionDataComponent;
import com.dtteam.dynamictrees.data.components.VoxelDataComponent;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.entity.LingeringEffectorEntity;
import com.dtteam.dynamictrees.item.DendroPotion;
import com.dtteam.dynamictrees.item.DirtBucket;
import com.dtteam.dynamictrees.item.Staff;
import com.dtteam.dynamictrees.loot.condition.SeasonalSeedDropChance;
import com.dtteam.dynamictrees.loot.condition.SpeciesMatches;
import com.dtteam.dynamictrees.loot.condition.VoluntarySeedDropChance;
import com.dtteam.dynamictrees.loot.entry.ItemBySpeciesLootPoolEntry;
import com.dtteam.dynamictrees.loot.entry.SeedItemLootPoolEntry;
import com.dtteam.dynamictrees.loot.entry.WeightedItemLootPoolEntry;
import com.dtteam.dynamictrees.loot.function.MultiplyByLogsCount;
import com.dtteam.dynamictrees.loot.function.MultiplyBySticksCount;
import com.dtteam.dynamictrees.loot.function.MultiplyCount;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.BranchConnectables;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.feature.CaveRootedTreeFeature;
import com.dtteam.dynamictrees.worldgen.feature.CaveRootedTreePlacement;
import com.dtteam.dynamictrees.worldgen.feature.DTReplaceNyliumFungiBlockStateProvider;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import com.dtteam.dynamictrees.worldgen.structure.DTCancelVanillaTreePoolElement;
import com.dtteam.dynamictrees.worldgen.structure.TreePoolElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DTRegistries {

    public static void setup(){
        setupConnectables();
    }
    public static final LinkedList<Item> CREATIVE_TAB_ITEMS = new LinkedList<>();

    ///////////////////////////////////////////
    // BLOCKS
    ///////////////////////////////////////////

    /**
     * A potted sapling block, which is a normal pot but for dynamic saplings.
     */
    public static final Supplier<PottedSaplingBlock> POTTED_SAPLING = Services.REGISTRY.getRegistryLoader()
            .registerBlock("potted_sapling", PottedSaplingBlock::new);

    /**
     * A trunk shell block, which is the outer block for thick branches.
     */
    public static final Supplier<TrunkShellBlock> TRUNK_SHELL = Services.REGISTRY.getRegistryLoader()
            .registerBlock("trunk_shell", TrunkShellBlock::new);

    private static void setupConnectables() {
        BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state, level, pos, side) -> {
            if (side == Direction.DOWN) return 1;
            return 0;
        });

        BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state, level, pos, side) -> {
            if (side == Direction.DOWN) {
                BlockState branchState = level.getBlockState(pos.relative(Direction.UP));
                BranchBlock branch = TreeHelper.getBranch(branchState);
                if (branch != null)
                    return Mth.clamp(branch.getRadius(branchState) - 1, 1, 8);
                else return 8;
            }
            return 0;
        });
    }

    ///////////////////////////////////////////
    // ITEMS
    ///////////////////////////////////////////

    /**
     * A custom potion called the Dendro Potion, houses all tree potions.
     */
    public static final Supplier<DendroPotion> DENDRO_POTION = Services.REGISTRY.getRegistryLoader()
            .registerItem("dendro_potion", DendroPotion::new);

    /**
     * A bucket of dirt item, for crafting saplings into seeds and vice versa.
     */
    public static final Supplier<DirtBucket> DIRT_BUCKET = Services.REGISTRY.getRegistryLoader()
            .registerItem("dirt_bucket", DirtBucket::new);

    /**
     * A staff, a creative tool for copying and pasting tree shapes.
     */
    public static final Supplier<Staff> STAFF = Services.REGISTRY.getRegistryLoader()
            .registerItem("staff", Staff::new);

    ///////////////////////////////////////////
    // CREATIVE TAB
    ///////////////////////////////////////////

    public static final Supplier<CreativeModeTab> DT_CREATIVE_TAB = Services.REGISTRY.getRegistryLoader()
            .registerCreativeTab(DynamicTrees.MOD_ID, ()-> Species.findSpecies(DynamicTrees.OAK).getSeedStack(1),
                    Component.translatable("itemGroup.dynamictrees"),
                    (_, output) -> {
                        for (final DendroPotion.DendroPotionType potion : DendroPotion.DendroPotionType.values()) {
                            if (potion.isActive()) {
                                output.accept(DendroPotion.applyIndexTag(new ItemStack(DENDRO_POTION.get()), potion.getIndex()));
                            }
                        }
                        CREATIVE_TAB_ITEMS.forEach(e -> output.accept(e.getDefaultInstance()));
                    });

    ///////////////////////////////////////////
    // ENTITIES
    ///////////////////////////////////////////

    public static final Supplier<EntityType<@NotNull FallingTreeEntity>> FALLING_TREE = Services.REGISTRY.getRegistryLoader()
            .registerEntity("falling_tree", EntityType.Builder.of(FallingTreeEntity::new, MobCategory.MISC), true);

    public static final Supplier<EntityType<@NotNull LingeringEffectorEntity>> LINGERING_EFFECTOR = Services.REGISTRY.getRegistryLoader()
            .registerEntity("lingering_effector", EntityType.Builder.of(LingeringEffectorEntity::new, MobCategory.MISC), false);

    ///////////////////////////////////////////
    // TILE ENTITIES
    ///////////////////////////////////////////

    public static Supplier<BlockEntityType<@NotNull SpeciesBlockEntity>> SPECIES_BLOCK_ENTITY = Services.REGISTRY.getRegistryLoader()
            .registerBlockEntity("tile_entity_species", SpeciesBlockEntity::new, getAllRootyBlocks());
    public static Supplier<BlockEntityType<@NotNull PottedSaplingBlockEntity>> POTTED_SAPLING_BLOCK_ENTITY = Services.REGISTRY.getRegistryLoader()
            .registerBlockEntity("potted_sapling", Services.REGISTRY.getPottedSaplingBlockEntity(), ()->Set.of(POTTED_SAPLING.get()));

    public static Supplier<Set<Block>> getAllRootyBlocks(){
        return ()->SoilProperties.REGISTRY.getAll().stream()
                .map(SoilProperties::getBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    ///////////////////////////////////////////
    // SOUNDS
    ///////////////////////////////////////////

    public static final Supplier<SoundEvent> FALLING_TREE_HIT_WATER = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_hit_water");
    public static final Supplier<SoundEvent> FALLING_TREE_BIG_START = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_big_start");
    public static final Supplier<SoundEvent> FALLING_TREE_BIG_END = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_big_end");
    public static final Supplier<SoundEvent> FALLING_TREE_MEDIUM_START = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_medium_start");
    public static final Supplier<SoundEvent> FALLING_TREE_MEDIUM_END = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_medium_end");
    public static final Supplier<SoundEvent> FALLING_TREE_SMALL_HIT_WATER = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_small_hit_water");
    public static final Supplier<SoundEvent> FALLING_TREE_SMALL_END = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_small_end");
    public static final Supplier<SoundEvent> FALLING_TREE_SMALL_END_BARE = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_small_end_bare");
    public static final Supplier<SoundEvent> FALLING_TREE_FUNGUS_START = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_fungus_start");
    public static final Supplier<SoundEvent> FALLING_TREE_FUNGUS_END = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_fungus_end");
    public static final Supplier<SoundEvent> FALLING_TREE_FUNGUS_SMALL_END = Services.REGISTRY.getRegistryLoader().registerSoundEvent("falling_tree_fungus_small_end");

    ///////////////////////////////////////////
    // DATA COMPONENTS
    ///////////////////////////////////////////

    public static final Supplier<DataComponentType<DyedItemColor>> STAFF_HANDLE_COLOR_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("handle_color", builder -> builder.persistent(DyedItemColor.CODEC).networkSynchronized(DyedItemColor.STREAM_CODEC));
    public static final Supplier<DataComponentType<DyedItemColor>> STAFF_CRYSTAL_COLOR_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("crystal_color", builder -> builder.persistent(DyedItemColor.CODEC).networkSynchronized(DyedItemColor.STREAM_CODEC));
    public static final Supplier<DataComponentType<String>> JOCODE_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("code", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final Supplier<DataComponentType<String>> ROOTS_JOCODE_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("roots_code", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final Supplier<DataComponentType<Unit>> READ_ONLY_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("read_only", builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final Supplier<DataComponentType<String>> SPECIES_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("species", builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final Supplier<DataComponentType<Integer>> DENDRO_POTION_INDEX_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("potion_index", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    public static final Supplier<DataComponentType<BranchDestructionData>> BRANCH_DESTRUCTION_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("branch_destruction_data", builder -> builder.persistent(BranchDestructionDataComponent.CODEC));//.networkSynchronized()
    public static final Supplier<DataComponentType<VoxelDataComponent>> VOXEL_DATA_COMPONENT = Services.REGISTRY.getRegistryLoader().
            registerDataComponentType("voxel_data", builder -> builder.persistent(VoxelDataComponent.CODEC));//.networkSynchronized()

    ///////////////////////////////////////////
    // ENTITY DATA SERIALIZERS
    ///////////////////////////////////////////

    public static final Supplier<EntityDataSerializer<VoxelDataComponent>> VOXEL_DATA_ENTITY_SERIALIZER = Services.REGISTRY.getRegistryLoader().
            registerEntityDataSerializer("voxel_data", () -> EntityDataSerializer.forValueType(VoxelDataComponent.STREAM_CODEC));

    ///////////////////////////////////////////
    // COMMAND ARGUMENTS
    ///////////////////////////////////////////

    public static final Supplier<SingletonArgumentInfo<@NotNull HexColorArgument>> HEX_COLOR = Services.REGISTRY.getRegistryLoader()
            .registerCommandArgumentType("hex_color", HexColorArgument.class, SingletonArgumentInfo.contextFree(HexColorArgument::hex));

    ///////////////////////////////////////////
    // LOOT
    ///////////////////////////////////////////

    public static final Supplier<MapCodec<SpeciesMatches>> SPECIES_MATCHES = Services.REGISTRY.getRegistryLoader()
            .registerLootConditionType("species_matches", SpeciesMatches.CODEC);
    public static final Supplier<MapCodec<SeasonalSeedDropChance>> SEASONAL_SEED_DROP_CHANCE = Services.REGISTRY.getRegistryLoader()
            .registerLootConditionType("seasonal_seed_drop_chance", SeasonalSeedDropChance.CODEC);
    public static final Supplier<MapCodec<VoluntarySeedDropChance>> VOLUNTARY_SEED_DROP_CHANCE = Services.REGISTRY.getRegistryLoader()
            .registerLootConditionType("voluntary_seed_drop_chance", VoluntarySeedDropChance.CODEC);

    public static final Supplier<MapCodec<ItemBySpeciesLootPoolEntry>> ITEM_BY_SPECIES = Services.REGISTRY.getRegistryLoader()
            .registerLootPoolEntryType("item_by_species", ItemBySpeciesLootPoolEntry.CODEC);
    public static final Supplier<MapCodec<SeedItemLootPoolEntry>> SEED_ITEM = Services.REGISTRY.getRegistryLoader()
            .registerLootPoolEntryType("seed_item", SeedItemLootPoolEntry.CODEC);
    public static final Supplier<MapCodec<WeightedItemLootPoolEntry>> WEIGHTED_ITEM = Services.REGISTRY.getRegistryLoader()
            .registerLootPoolEntryType("weighted_item", WeightedItemLootPoolEntry.CODEC);

    public static final Supplier<MapCodec<MultiplyCount>> MULTIPLY_COUNT = Services.REGISTRY.getRegistryLoader()
            .registerLootFunctionType("multiply_count", MultiplyCount.CODEC);
    public static final Supplier<MapCodec<MultiplyByLogsCount>> MULTIPLY_LOGS_COUNT = Services.REGISTRY.getRegistryLoader()
            .registerLootFunctionType("multiply_logs_count", MultiplyByLogsCount.CODEC);
    public static final Supplier<MapCodec<MultiplyBySticksCount>> MULTIPLY_STICKS_COUNT = Services.REGISTRY.getRegistryLoader()
            .registerLootFunctionType("multiply_sticks_count", MultiplyBySticksCount.CODEC);

    ///////////////////////////////////////////
    // WORLDGEN
    ///////////////////////////////////////////

    public static final ResourceKey<ConfiguredFeature<?, ?>> DYNAMIC_TREE_CONFIGURED_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, DynamicTrees.location("dynamic_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_ROOTED_TREE_CONFIGURED_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE,DynamicTrees.location("cave_rooted_tree"));
    public static final ResourceKey<PlacedFeature> DYNAMIC_TREE_PLACED_FEATURE = ResourceKey.create(Registries.PLACED_FEATURE,DynamicTrees.location("dynamic_tree"));
    /** Placement for trees that generate on the surface above the target biome. This is used for trees like the azalea. */
    public static final ResourceKey<PlacedFeature> CAVE_ROOTED_TREE_PLACED_FEATURE = ResourceKey.create(Registries.PLACED_FEATURE,DynamicTrees.location("cave_rooted_tree"));

    public static final Supplier<PlacementModifierType<@NotNull CaveRootedTreePlacement>> CAVE_ROOTED_TREE_PLACEMENT_MODIFIER_TYPE = Services.REGISTRY.getRegistryLoader()
            .registerPlacementModifierType("cave_rooted_tree", () -> () -> CaveRootedTreePlacement.CODEC);

    public static final Supplier<DynamicTreeFeature> DYNAMIC_TREE_FEATURE = Services.REGISTRY.getRegistryLoader()
            .registerFeature("tree", DynamicTreeFeature::new);
    public static final Supplier<CaveRootedTreeFeature> CAVE_ROOTED_TREE_FEATURE = Services.REGISTRY.getRegistryLoader()
            .registerFeature("cave_rooted_tree", CaveRootedTreeFeature::new);

    public static final Supplier<BlockStateProviderType<@NotNull DTReplaceNyliumFungiBlockStateProvider>> REPLACE_NYLIUM_FUNGI_BLOCK_STATE_PROVIDER_TYPE = Services.REGISTRY.getRegistryLoader()
            .registerBlockStateProviderType("replace_nylium_fungi", () -> new BlockStateProviderType<>(DTReplaceNyliumFungiBlockStateProvider.CODEC));

    public static final Supplier<StructurePoolElementType<@NotNull DTCancelVanillaTreePoolElement>> CANCEL_VANILLA_VILLAGE_TREE_STRUCTURE_POOL_ELEMENT_TYPE = Services.REGISTRY.getRegistryLoader()
            .registerStructurePoolElementType("cancel_vanilla_village_tree_element", () -> () -> DTCancelVanillaTreePoolElement.CODEC);

    public static final Supplier<StructurePoolElementType<@NotNull TreePoolElement>> TREE_STRUCTURE_POOL_ELEMENT_TYPE = Services.REGISTRY.getRegistryLoader()
            .registerStructurePoolElementType("tree_pool_element", () -> () -> TreePoolElement.CODEC);

//    public static final Supplier<RecipeSerializer<SeedConversionRecipe>> SEED_CONVERSION_RECIPE_TYPE = Services.REGISTRY.getRegistryLoader()
//            .registerRecipeType("seed_conversion", ()->new SimpleCraftingRecipeSerializer<>(SeedConversionRecipe::new));
//    public static final Supplier<RecipeSerializer<MegaSeedRecipe>> MEGA_SEED_RECIPE_TYPE = Services.REGISTRY.getRegistryLoader()
//            .registerRecipeType("mega_seed", ()->new SimpleCraftingRecipeSerializer<>(MegaSeedRecipe::new));
}
