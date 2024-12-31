package com.dtteam.dynamictrees.init;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.block.soil.RootyBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpeciesBlockEntity;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.item.DendroPotion;
import com.dtteam.dynamictrees.item.DirtBucket;
import com.dtteam.dynamictrees.item.Staff;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.systems.BranchConnectables;
import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
            .registerBlock(DynamicTrees.location("potted_sapling"), PottedSaplingBlock::new);

    /**
     * A trunk shell block, which is the outer block for thick branches.
     */
    public static final Supplier<TrunkShellBlock> TRUNK_SHELL = Services.REGISTRY.getRegistryLoader()
            .registerBlock(DynamicTrees.location("trunk_shell"), TrunkShellBlock::new);

    private static void setupConnectables() {
        BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state, level, pos, side) -> {
            if (side == Direction.DOWN) {
                return 1;
            }
            return 0;
        });

        BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state, level, pos, side) -> {
            if (side == Direction.DOWN) {
                BlockState branchState = level.getBlockState(pos.relative(Direction.UP));
                BranchBlock branch = TreeHelper.getBranch(branchState);
                if (branch != null) {
                    return Mth.clamp(branch.getRadius(branchState) - 1, 1, 8);
                } else {
                    return 8;
                }
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
            .registerItem(DynamicTrees.location("dendro_potion"), DendroPotion::new);

    /**
     * A bucket of dirt item, for crafting saplings into seeds and vice versa.
     */
    public static final Supplier<DirtBucket> DIRT_BUCKET = Services.REGISTRY.getRegistryLoader()
            .registerItem(DynamicTrees.location("dirt_bucket"), DirtBucket::new);

    /**
     * A staff, a creative tool for copying and pasting tree shapes.
     */
    public static final Supplier<Staff> STAFF = Services.REGISTRY.getRegistryLoader()
            .registerItem(DynamicTrees.location("staff"), Staff::new);

    ///////////////////////////////////////////
    // CREATIVE TAB
    ///////////////////////////////////////////

    public static final LinkedList<Item> CREATIVE_TAB_ITEMS = new LinkedList<>();
    public static final Supplier<CreativeModeTab> DT_CREATIVE_TAB = Services.REGISTRY.getRegistryLoader()
            .registerCreativeTab(DynamicTrees.MOD_ID,
                    new ItemStack(Items.STICK),
                    Component.translatable("itemGroup.dynamictrees"),
                    (parameters, output) -> {
                        CREATIVE_TAB_ITEMS.forEach(e -> output.accept(e.getDefaultInstance()));
                        for (final DendroPotion.DendroPotionType potion : DendroPotion.DendroPotionType.values()) {
                            if (potion.isActive()) {
                                output.accept(DendroPotion.applyIndexTag(new ItemStack(DENDRO_POTION.get()), potion.getIndex()));
                            }
                        }
                    });
    //TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1))

    ///////////////////////////////////////////
    // ENTITIES
    ///////////////////////////////////////////

    public static final Supplier<EntityType<FallingTreeEntity>> FALLING_TREE = Services.REGISTRY.getRegistryLoader().registerEntity("falling_tree", EntityType.Builder.of(FallingTreeEntity::new, MobCategory.MISC), true);

//    public static final Supplier<EntityType<LingeringEffectorEntity>> LINGERING_EFFECTOR = registerEntity("lingering_effector", () -> EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, MobCategory.MISC)
//            .setCustomClientFactory((spawnEntity, level) ->
//                    new LingeringEffectorEntity(level, BlockPos.containing(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null)));


    ///////////////////////////////////////////
    // TILE ENTITIES
    ///////////////////////////////////////////

    public static Supplier<BlockEntityType<SpeciesBlockEntity>> SPECIES_BLOCK_ENTITY = Services.REGISTRY.getRegistryLoader()
            .registerBlockEntity("tile_entity_species", SpeciesBlockEntity::new, getAllRootyBlocks());
    public static Supplier<BlockEntityType<PottedSaplingBlockEntity>> POTTED_SAPLING_BLOCK_ENTITY = Services.REGISTRY.getRegistryLoader()
            .registerBlockEntity("potted_sapling", PottedSaplingBlockEntity::new, ()->Set.of(POTTED_SAPLING.get()));

    public static Supplier<Set<Block>> getAllRootyBlocks(){
        return ()->SoilProperties.REGISTRY.getAll().stream()
                .map(SoilProperties::getBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

}
