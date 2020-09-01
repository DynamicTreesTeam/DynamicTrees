package com.ferreusveritas.dynamictrees.trees;

import java.util.List;
import java.util.function.BiFunction;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenClearVolume;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenCocoa;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenFlareBottom;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenMound;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenRoots;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenUndergrowth;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenVine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

public class TreeJungle extends TreeFamilyVanilla {
	
	public class SpeciesJungle extends Species {
		
		SpeciesJungle(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Jungle Trees are tall, wildly growing, fast growing trees with low branches to provide inconvenient obstruction and climbing
			setBasicGrowingParameters(0.2f, 28.0f, 3, 2, 1.0f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(ModTrees.JUNGLE));
			
			envFactor(Type.COLD, 0.15f);
			envFactor(Type.DRY,  0.20f);
			envFactor(Type.HOT, 1.1f);
			envFactor(Type.WET, 1.1f);
			
			setupStandardSeedDropping();
			
			//Add species features
			addGenFeature(new FeatureGenCocoa());
			addGenFeature(new FeatureGenVine().setQuantity(16).setMaxLength(16));
			addGenFeature(new FeatureGenUndergrowth());
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return BiomeDictionary.hasType(biome, Type.JUNGLE);
		};
		
	}
	
	
	public class SpeciesMegaJungle extends SpeciesRare {
		
		private static final String speciesName = "megajungle";
		
		SpeciesMegaJungle(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getResourceDomain(), speciesName), treeFamily);

			setBasicGrowingParameters(0.32f, 32.0f, 7, 8, 0.9f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(ModTrees.JUNGLE));
			
			envFactor(Type.COLD, 0.15f);
			envFactor(Type.DRY,  0.20f);
			envFactor(Type.HOT, 1.1f);
			envFactor(Type.WET, 1.1f);
			
			setSoilLongevity(16);//Grows for a while so it can actually get tall
			
			//Add species features
			addGenFeature(new FeatureGenVine().setQuantity(16).setMaxLength(16));
			addGenFeature(new FeatureGenFlareBottom());//Flare the bottom
			addGenFeature(new FeatureGenClearVolume(8));//Clear a spot for the thick tree trunk
			addGenFeature(new FeatureGenMound(999));//Place a 3x3 of dirt under thick trees
			addGenFeature(new FeatureGenRoots(9).setScaler(getRootScaler()));//Finally Generate Roots
		}
		
		protected BiFunction<Integer, Integer, Integer> getRootScaler() {
			return (inRadius, trunkRadius) -> {
				float scale = MathHelper.clamp(trunkRadius >= 9 ? (trunkRadius / 18f) : 0, 0, 1);
				return (int) (inRadius * scale);
			};
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return BiomeDictionary.hasType(biome, Type.JUNGLE);
		};
		
		/*protected BiFunction<Integer, Integer, Integer> getRootScaler() {
			return (inRadius, trunkRadius) -> {
				float scale = MathHelper.clamp(trunkRadius >= 13 ? (trunkRadius / 24f) : 0, 0, 1);
				return (int) (inRadius * scale);
			};
		}*/
		
		//Mega jungle are just jungle trees under special circumstances..  So they have the same seeds
		@Override
		public ItemStack getSeedStack(int qty) {
			return getCommonSpecies().getSeedStack(qty);
		}
		
		//Mega jungle are just jungle trees under special circumstances..  So they have the same seeds
		@Override
		public Seed getSeed() {
			return getCommonSpecies().getSeed();
		}
		
		@Override
		public int maxBranchRadius() {
			return 24;
		}
		
		@Override
		public boolean isThick() {
			return true;
		}
		
	}
	
	Species megaSpecies;
	BlockSurfaceRoot surfaceRootBlock;
	
	public TreeJungle() {
		super(BlockPlanks.EnumType.JUNGLE);
		canSupportCocoa = true;
		
		surfaceRootBlock = new BlockSurfaceRoot(Material.WOOD, getName() + "root");
		
		addConnectableVanillaLeaves((state) -> { return state.getBlock() instanceof BlockOldLeaf && (state.getValue(BlockOldLeaf.VARIANT) == BlockPlanks.EnumType.JUNGLE); });
	}
	
	
	@Override
	public void createSpecies() {
		megaSpecies = new SpeciesMegaJungle(this);
		setCommonSpecies(new SpeciesJungle(this));
	}
	
	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		speciesRegistry.register(megaSpecies);
	}
	
	@Override
	public boolean isThick() {
		return true;
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
	
	@Override
	public boolean onTreeActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
	
		//Place Cocoa Pod if we are holding Cocoa Beans
		if(heldItem != null) {
			if(heldItem.getItem() == Items.DYE && heldItem.getItemDamage() == 3) {
				BlockBranch branch = TreeHelper.getBranch(state);
				if(branch != null && branch.getRadius(state) == 8) {
					if(side != EnumFacing.UP && side != EnumFacing.DOWN) {
						pos = pos.offset(side);
					}
					if (world.isAirBlock(pos)) {
						IBlockState cocoaState = ModBlocks.blockFruitCocoa.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, 0, player);
						EnumFacing facing = cocoaState.getValue(BlockHorizontal.FACING);
						world.setBlockState(pos, ModBlocks.blockFruitCocoa.getDefaultState().withProperty(BlockHorizontal.FACING, facing), 2);
						if (!player.capabilities.isCreativeMode) {
							heldItem.shrink(1);
						}
						return true;
					}
				}
			}
		}
		
		//Need this here to apply potions or bone meal.
		return super.onTreeActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
	}
	
}
