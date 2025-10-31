package com.dtteam.dynamictrees.tree.species;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatures;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.NetherFungusFamily;
import com.dtteam.dynamictrees.block.CommonVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static com.dtteam.dynamictrees.utility.ResourceLocationUtils.surround;

/**
 * @author Harley O'Connor
 */
public class NetherFungusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(NetherFungusSpecies::new);

    public NetherFungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        if (!(family instanceof NetherFungusFamily)) {
            LogManager.getLogger().warn("Family {} for nether fungus species {} is not of type {}", family.getRegistryName(), getRegistryName(), NetherFungusFamily.class);
        }
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
        return this.setDefaultGrowingParameters()
                .setSaplingSound(SoundType.FUNGUS)
                .setCanSaplingGrowNaturally(false)
//                .envFactor(Tags.Biomes.IS_COLD, 0.25f)
//                .envFactor(Tags.Biomes.IS_WET, 0.75f)
                ;
    }

    @Override
    public Species setPostReloadDefaults() {
        if (!this.hasGenFeatures()) {
            this.addGenFeature(GenFeatures.CLEAR_VOLUME).addGenFeature(GenFeatures.SHROOMLIGHT);
        }
        return super.setPostReloadDefaults();
    }

    @Override
    public boolean isAcceptableSoilForWorldgen(LevelAccessor level, BlockPos pos, BlockState soilBlockState) {
        if (soilBlockState.getBlock() == Blocks.NETHERRACK) {
            return true; //Soil exception for worldgen
        }
        return super.isAcceptableSoilForWorldgen(level, pos, soilBlockState);
    }

    @Override
    public float defaultSeedComposterChance() {
        return 0.65f;
    }

    @Override
    public List<TagKey<Block>> defaultSaplingTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_CAPS);
    }

    @Override
    public List<TagKey<Item>> defaultSeedTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_CAPS);
    }

    @Override
    public void addSaplingTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                   ResourceLocation leavesTextureLocation, ResourceLocation barkTextureLocation) {
        ResourceLocation capLoc = getTexturePath(SAPLING).orElse(surround(this.getRegistryName(), "block/", "_cap"));
        textureConsumer.accept("stem", capLoc);
        textureConsumer.accept("cap", capLoc);
    }

    @Override
    public float falloverParticleFlingMultiplier() {
        return 0.5f;
    }

    public SoundEvent getFallingTreeStartSound (float treeVolume, boolean hasLeaves){
        return DTRegistries.FALLING_TREE_FUNGUS_START.get();
    }

    public SoundEvent getFallingTreeEndSound (float treeVolume, boolean hasLeaves){
        return DTRegistries.FALLING_TREE_FUNGUS_END.get();
    }

    public SoundEvent getFallingBranchEndSound (float treeVolume, boolean hasLeaves, boolean fellOnWater){
        return  hasLeaves ? DTRegistries.FALLING_TREE_FUNGUS_SMALL_END.get() : DTRegistries.FALLING_TREE_SMALL_END_BARE.get();
    }

    public float getFallingTreePitch (float treeVolume){
        return 1.5f/(1+treeVolume*0.04f);
    }

}
