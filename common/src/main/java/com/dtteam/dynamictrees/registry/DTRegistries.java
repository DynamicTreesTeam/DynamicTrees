package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpeciesBlockEntity;
import com.dtteam.dynamictrees.command.HexColorArgument;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.entity.LingeringEffectorEntity;
import com.dtteam.dynamictrees.item.DendroPotion;
import com.dtteam.dynamictrees.item.DirtBucket;
import com.dtteam.dynamictrees.item.Staff;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.BranchConnectables;
import com.dtteam.dynamictrees.util.TreeHelper;
import com.dtteam.dynamictrees.util.TreeRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DTRegistries {

    public static void setup(){
        setupConnectables();
    }

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

    public static final LinkedList<Item> CREATIVE_TAB_ITEMS = new LinkedList<>();
    public static final Supplier<CreativeModeTab> DT_CREATIVE_TAB = Services.REGISTRY.getRegistryLoader()
            .registerCreativeTab(DynamicTrees.MOD_ID,
                    ()->TreeRegistry.findSpecies(DynamicTrees.OAK).getSeedStack(1),
                    Component.translatable("itemGroup.dynamictrees"),
                    (parameters, output) -> {
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

    public static final Supplier<EntityType<FallingTreeEntity>> FALLING_TREE = Services.REGISTRY.getRegistryLoader()
            .registerEntity("falling_tree", EntityType.Builder.of(FallingTreeEntity::new, MobCategory.MISC), true);

    public static final Supplier<EntityType<LingeringEffectorEntity>> LINGERING_EFFECTOR = Services.REGISTRY.getRegistryLoader()
            .registerEntity("lingering_effector", EntityType.Builder.of(LingeringEffectorEntity::new, MobCategory.MISC), false);
//            .setCustomClientFactory((spawnEntity, level) ->
//                    new LingeringEffectorEntity(level, BlockPos.containing(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null))

    ///////////////////////////////////////////
    // TILE ENTITIES
    ///////////////////////////////////////////

    public static Supplier<BlockEntityType<SpeciesBlockEntity>> SPECIES_BLOCK_ENTITY = Services.REGISTRY.getRegistryLoader()
            .registerBlockEntity("tile_entity_species", SpeciesBlockEntity::new, getAllRootyBlocks());
    public static Supplier<BlockEntityType<PottedSaplingBlockEntity>> POTTED_SAPLING_BLOCK_ENTITY = Services.REGISTRY.getRegistryLoader()
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

    ///////////////////////////////////////////
    // COMMAND ARGUMENTS
    ///////////////////////////////////////////

    public static final Supplier<SingletonArgumentInfo<HexColorArgument>> HEX_COLOR = Services.REGISTRY.getRegistryLoader()
            .registerCommandArgumentType("hex_color", HexColorArgument.class, SingletonArgumentInfo.contextFree(HexColorArgument::hex));

}
