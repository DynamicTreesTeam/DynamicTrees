package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.genfeatures.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Optional;
import java.util.function.BiFunction;

public class JungleTree extends VanillaTreeFamily {
	
	public class JungleSpecies extends Species {
		
		JungleSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Jungle Trees are tall, wildly growing, fast growing trees with low branches to provide inconvenient obstruction and climbing
			setBasicGrowingParameters(0.2f, 28.0f, 3, 2, 1.0f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(DTTrees.JUNGLE));
			
			envFactor(Type.COLD, 0.15f);
			envFactor(Type.DRY,  0.20f);
			envFactor(Type.HOT, 1.1f);
			envFactor(Type.WET, 1.1f);
			
			setupStandardSeedDropping();
			setupStandardStickDropping();
			
			//Add species features
			this.addGenFeature(GenFeatures.COCOA);
			this.addGenFeature(GenFeatures.VINES.with(VinesGenFeature.QUANTITY, 16).with(VinesGenFeature.MAX_LENGTH, 16));
			this.addGenFeature(GenFeatures.UNDERGROWTH);
		}
		
		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.JUNGLE);
		};

		@Override
		public Species getMegaSpecies() {
			return megaSpecies;
		}

	}

	public class MegaJungleSpecies extends Species {
		
		private static final String speciesName = "mega_jungle";
		
		MegaJungleSpecies(TreeFamily treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getNamespace(), speciesName), treeFamily);
			
			setRequiresTileEntity(true);
			
			setBasicGrowingParameters(0.32f, 32.0f, 7, 8, 0.9f);
			setGrowthLogicKit(TreeRegistry.findGrowthLogicKit(DTTrees.JUNGLE));
			
			envFactor(Type.COLD, 0.15f);
			envFactor(Type.DRY,  0.20f);
			envFactor(Type.HOT, 1.1f);
			envFactor(Type.WET, 1.1f);
			
			setSoilLongevity(16);//Grows for a while so it can actually get tall
			
			//Add species features
			this.addGenFeature(GenFeatures.VINES.getDefaultConfiguration().with(VinesGenFeature.QUANTITY, 16)
					.with(VinesGenFeature.MAX_LENGTH, 16));
			this.addGenFeature(GenFeatures.BOTTOM_FLARE.getDefaultConfiguration()); // Flare the bottom
			this.addGenFeature(GenFeatures.CLEAR_VOLUME.getDefaultConfiguration()); // Clear a spot for the thick tree trunk
			this.addGenFeature(GenFeatures.MOUND.getDefaultConfiguration().with(MoundGenFeature.MOUND_CUTOFF_RADIUS, 999)); // Place a 3x3 of dirt under thick trees
			this.addGenFeature(GenFeatures.ROOTS.getDefaultConfiguration().with(RootsGenFeature.MIN_TRUNK_RADIUS, 9)); // Finally Generate Roots
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.JUNGLE);
		};

		//Mega jungle are just jungle trees under special circumstances..  So they have the same seeds
		@Override
		public ItemStack getSeedStack(int qty) {
			return getCommonSpecies().getSeedStack(qty);
		}
		
		//Mega jungle are just jungle trees under special circumstances..  So they have the same seeds
		@Override
		public Optional<Seed> getSeed() {
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

		@Override
		public boolean isMega() {
			return true;
		}

	}
	
	Species megaSpecies;

	public JungleTree() {
		super(DynamicTrees.VanillaWoodTypes.jungle);
		canSupportCocoa = true;

		addConnectableVanillaLeaves((state) -> state.getBlock() == Blocks.JUNGLE_LEAVES);
	}
	
	@Override
	public void createSpecies() {
		megaSpecies = new MegaJungleSpecies(this);
		setCommonSpecies(new JungleSpecies(this));
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
	public boolean hasSurfaceRoot() {
		return true;
	}

	@Override
	public boolean onTreeActivated(World world, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, ItemStack heldItem, BlockRayTraceResult hit) {
		//Place Cocoa Pod if we are holding Cocoa Beans
		BlockPos pos = hit.getPos();
		if(heldItem != null) {
			if(heldItem.getItem() == Items.COCOA_BEANS) {
				BranchBlock branch = TreeHelper.getBranch(state);
				if(branch != null && branch.getRadius(state) == 8) {
					if(hit.getFace() != Direction.UP && hit.getFace() != Direction.DOWN) {
						pos = pos.offset(hit.getFace());
					}
					if (world.isAirBlock(pos)) {
						BlockState cocoaState = DTRegistries.cocoaFruitBlock.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, hand, hit)));
						assert cocoaState != null;
						Direction facing = cocoaState.get(HorizontalBlock.HORIZONTAL_FACING);
						world.setBlockState(pos, DTRegistries.cocoaFruitBlock.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, facing), 2);
						if (!player.isCreative()) {
							heldItem.shrink(1);
						}
						return true;
					}
				}
			}
		}

		//Need this here to apply potions or bone meal.
		return super.onTreeActivated(world, hitPos, state, player, hand, heldItem, hit);
	}
	
}
