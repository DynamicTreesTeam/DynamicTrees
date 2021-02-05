package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.FruitDropCreator;
import com.ferreusveritas.dynamictrees.systems.featuregen.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class DarkOakTree extends VanillaTreeFamily {
	
	public class DarkOakSpecies extends Species {
		
		protected HugeMushroomsGenFeature underGen;
		protected BottomFlareGenFeature flareBottomGen;
		protected RootsGenFeature rootGen;
		protected MoundGenFeature moundGen;
		
		DarkOakSpecies(TreeFamily treeFamily) {
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
				addDropCreator(new FruitDropCreator());
			}
			
			setupStandardSeedDropping();
			setupStandardStickDropping();
			
			//Add species features
			addGenFeature(new ClearVolumeGenFeature(6));//Clear a spot for the thick tree trunk
			addGenFeature(new BottomFlareGenFeature());//Flare the bottom
			addGenFeature(new MoundGenFeature(5));//Establish mounds
			addGenFeature(new PredicateGenFeature(
				new HugeMushroomsGenFeature().setMaxShrooms(1).setMaxAttempts(3)//Generate Huge Mushrooms
				).setBiomePredicate(biome -> biome == ForgeRegistries.BIOMES.getValue(Biomes.DARK_FOREST.getRegistryName()) || biome == ForgeRegistries.BIOMES.getValue(Biomes.DARK_FOREST_HILLS.getRegistryName()))//Only allow this feature in roofed forests
			);
			addGenFeature(new RootsGenFeature(13).setScaler(getRootScaler()));//Finally Generate Roots
		}
		
		protected BiFunction<Integer, Integer, Integer> getRootScaler() {
			return (inRadius, trunkRadius) -> {
				float scale = MathHelper.clamp(trunkRadius >= 13 ? (trunkRadius / 24f) : 0, 0, 1);
				return (int) (inRadius * scale);
			};
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
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
		public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 2 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(LightType.SKY, pos) < 6) {
					world.setBlockState(pos, DTRegistries.blockStates.redMushroom, 3);//Change branch to a red mushroom
					world.setBlockState(pos.down(), DTRegistries.blockStates.podzol, 3);//Change rooty dirt to Podzol
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

	public DarkOakTree() {
		super(DynamicTrees.VanillaWoodTypes.dark_oak);
		hasConiferVariants = true;

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.DARK_OAK_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new DarkOakSpecies(this));
	}
	
	@Override
	public boolean isThick() {
		return true;
	}

	@Override
	public boolean hasSurfaceRoot() {
		return true;
	}

}
