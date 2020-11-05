package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorFruit;
import com.ferreusveritas.dynamictrees.systems.featuregen.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary.Type;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class TreeDarkOak extends TreeFamilyVanilla {
	
	public class SpeciesDarkOak extends Species {
		
		protected FeatureGenHugeMushrooms underGen;
		protected FeatureGenFlareBottom flareBottomGen;
		protected FeatureGenRoots rootGen;
		protected FeatureGenMound moundGen;
		
		SpeciesDarkOak(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Dark Oak Trees are tall, slowly growing, thick trees
			setBasicGrowingParameters(0.30f, 18.0f, 4, 6, 0.8f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(DTTrees.DARK_OAK));
			
			setSoilLongevity(14);//Grows for a long long time
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.25f);
			envFactor(Type.MUSHROOM, 1.25f);
			
			if(DTConfigs.worldGen.get() && !DTConfigs.enableAppleTrees.get()) {
				addDropCreator(new DropCreatorFruit());
			}
			
			setupStandardSeedDropping();
			
			//Add species features
			addGenFeature(new FeatureGenClearVolume(6));//Clear a spot for the thick tree trunk
			addGenFeature(new FeatureGenFlareBottom());//Flare the bottom
			addGenFeature(new FeatureGenMound(5));//Establish mounds
			addGenFeature(new FeatureGenPredicate(
				new FeatureGenHugeMushrooms().setMaxShrooms(1).setMaxAttempts(3)//Generate Huge Mushrooms
				).setBiomePredicate(biome -> biome == Biomes.DARK_FOREST || biome == Biomes.DARK_FOREST_HILLS)//Only allow this feature in roofed forests
			);
			addGenFeature(new FeatureGenRoots(13).setScaler(getRootScaler()));//Finally Generate Roots
		}
		
		protected BiFunction<Integer, Integer, Integer> getRootScaler() {
			return (inRadius, trunkRadius) -> {
				float scale = MathHelper.clamp(trunkRadius >= 13 ? (trunkRadius / 24f) : 0, 0, 1);
				return (int) (inRadius * scale);
			};
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.DARK_FOREST, Biomes.DARK_FOREST_HILLS);
		};
		
		@Override
		public int getLowestBranchHeight(World world, BlockPos pos) {
			return (int)(super.getLowestBranchHeight(world, pos) * biomeSuitability(world, pos));
		}
		
		@Override
		public float getEnergy(World world, BlockPos pos) {
			return super.getEnergy(world, pos) * biomeSuitability(world, pos);
		}
		
		@Override
		public float getGrowthRate(World world, BlockPos pos) {
			return super.getGrowthRate(world, pos) * biomeSuitability(world, pos);
		}
		
		@Override
		public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 2 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(LightType.SKY, pos) < 6) {
					world.setBlockState(pos, DTRegistries.blockStates.redMushroom);//Change branch to a red mushroom
					world.setBlockState(pos.down(), DTRegistries.blockStates.podzol);//Change rooty dirt to Podzol
				}
				return true;
			}
			
			return false;
		}
		
		@Override
		public boolean isThick() {
			return true;
		}
		
	}
	
	BlockSurfaceRoot surfaceRootBlock;
	
	public TreeDarkOak() {
		super(DynamicTrees.VanillaWoodTypes.dark_oak);
		hasConiferVariants = true;
		
		surfaceRootBlock = new BlockSurfaceRoot(Material.WOOD, getName() + "_root");

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.DARK_OAK_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesDarkOak(this));
	}
	
	@Override
	public boolean isThick() {
		return false;
	}
	
	@Override
	public boolean autoCreateBranch() {
		return true;
	}
	
	@Override
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		blockList = super.getRegisterableBlocks(blockList);
		blockList.add(surfaceRootBlock);
		return blockList;
	}
	
	@Override
	public BlockSurfaceRoot getSurfaceRoots() {
		return surfaceRootBlock;
	}
	
}
