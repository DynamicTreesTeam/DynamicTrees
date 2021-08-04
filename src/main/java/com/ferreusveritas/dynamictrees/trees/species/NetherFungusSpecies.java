package com.ferreusveritas.dynamictrees.trees.species;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilHelper;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.systems.dropcreators.ConfiguredDropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class NetherFungusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(NetherFungusSpecies::new);

    public NetherFungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        this.setSaplingShape(CommonVoxelShapes.SAPLING);

        addDropCreators(new DropCreator(new ResourceLocation(DynamicTrees.MOD_ID, "wart_block_drop")) {
            @Override
            protected void registerProperties() {

            }

            @Override
            public void appendHarvestDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
                int chance = 10;
                if (context.fortune() > 0) {
                    chance -= 2 << context.fortune();
                    if (chance < 10) {
                        chance = 5;
                    }
                }
                if (context.random().nextInt(chance) == 0) {
                    ItemStack drop = context.species().getLeavesProperties().getPrimitiveLeavesItemStack().copy();
                    context.drops().add(drop);
                }
            }
        });
    }

    @Override
    public Species setDefaultGrowingParameters() {
        this.setBasicGrowingParameters(0f, 14.0f, 0, 4, 1f);
        return super.setDefaultGrowingParameters();
    }

    @Override
    protected void setStandardSoils() {
        this.addAcceptableSoils(SoilHelper.NETHER_SOIL_LIKE, SoilHelper.FUNGUS_LIKE, SoilHelper.DIRT_LIKE);
    }

    @Override
    public Species setPreReloadDefaults() {
        return super.setPreReloadDefaults().setSaplingSound(SoundType.FUNGUS).setCanSaplingGrowNaturally(false).setSaplingShape(CommonVoxelShapes.FLAT_MUSHROOM)
                .envFactor(BiomeDictionary.Type.COLD, 0.25f).envFactor(BiomeDictionary.Type.WET, 0.75f);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (!this.hasGenFeatures()) {
            this.addGenFeature(GenFeatures.CLEAR_VOLUME).addGenFeature(GenFeatures.SHROOMLIGHT);
        }
        return super.setPostReloadDefaults();
    }

    @Override
    public boolean isAcceptableSoilForWorldgen(IWorld world, BlockPos pos, BlockState soilBlockState) {
        if (soilBlockState.getBlock() == Blocks.NETHERRACK) {
            return true; //Soil exception for worldgen
        }
        return super.isAcceptableSoilForWorldgen(world, pos, soilBlockState);
    }

    @Override
    public float defaultSeedComposterChance() {
        return 0.65f;
    }

    @Override
    public List<ITag.INamedTag<Block>> defaultSaplingTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_CAPS);
    }

    @Override
    public List<ITag.INamedTag<Item>> defaultSeedTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_CAPS);
    }

}
