package com.ferreusveritas.dynamictrees.trees.specialspecies;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.List;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public class NetherFungusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(NetherFungusSpecies::new);

    public NetherFungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        this.setSaplingShape(DTRegistries.FLAT_MUSHROOM);

        addDropCreator(new DropCreator(new ResourceLocation(DynamicTrees.MOD_ID, "wart_block_drop")){
            @Override
            public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
                int chance = 10;
                if (fortune > 0) {
                    chance -= 2 << fortune;
                    if (chance < 10)
                        chance = 5;
                }
                if(random.nextInt(chance) == 0) {
                    ItemStack drop = species.getLeavesProperties().getPrimitiveLeavesItemStack().copy();
                    dropList.add(drop);
                }
                return dropList;
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
        this.addAcceptableSoils(DirtHelper.NETHER_SOIL_LIKE, DirtHelper.FUNGUS_LIKE);
    }

    @Override
    public Species setPreReloadDefaults() {
        return super.setPreReloadDefaults().setSaplingSound(SoundType.FUNGUS).setCanSaplingGrowNaturally(false).setSaplingShape(DTRegistries.FLAT_MUSHROOM)
                .envFactor(BiomeDictionary.Type.COLD, 0.25f).envFactor(BiomeDictionary.Type.WET, 0.75f);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (!this.areAnyGenFeatures())
            this.addGenFeature(GenFeatures.CLEAR_VOLUME).addGenFeature(GenFeatures.SHROOMLIGHT);
        return super.setPostReloadDefaults();
    }

    @Override
    public boolean isAcceptableSoilForWorldgen(IWorld world, BlockPos pos, BlockState soilBlockState) {
        if (soilBlockState.getBlock() == Blocks.NETHERRACK) return true; //Soil exception for worldgen
        return super.isAcceptableSoilForWorldgen(world, pos, soilBlockState);
    }
}
