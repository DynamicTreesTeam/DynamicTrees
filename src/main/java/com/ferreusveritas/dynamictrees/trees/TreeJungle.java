package com.ferreusveritas.dynamictrees.trees;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenUndergrowth;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenVine;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFruitCocoa;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeJungle extends TreeFamily {
	
	public class SpeciesJungle extends Species {

		FeatureGenVine vineGen;
		FeatureGenUndergrowth underGen;
		
		SpeciesJungle(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.jungleLeavesProperties);

			//Jungle Trees are tall, wildly growing, fast growing trees with low branches to provide inconvenient obstruction and climbing
			setBasicGrowingParameters(0.2f, 28.0f, 3, 2, 1.0f);
			
			envFactor(Type.COLD, 0.15f);
			envFactor(Type.DRY,  0.20f);
			envFactor(Type.HOT, 1.1f);
			envFactor(Type.WET, 1.1f);
			
			setupStandardSeedDropping();
			
			vineGen = new FeatureGenVine(this);
			underGen = new FeatureGenUndergrowth(this);
		}

		@Override
		public boolean isBiomePerfect(Biome biome) {
			return CompatHelper.biomeHasType(biome, Type.JUNGLE);
		};
		
		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
			
			EnumFacing originDir = signal.dir.getOpposite();
			
			int treeHash = CoordUtils.coordHashCode(signal.rootPos, 2);
			int posHash = CoordUtils.coordHashCode(pos, 2);
			
			//Alter probability map for direction change
			probMap[0] = 0;//Down is always disallowed for jungle
			probMap[1] = signal.isInTrunk() ? getUpProbability(): 0;
			probMap[2] = probMap[3] = probMap[4] = probMap[5] = 0;
			int sideTurn = !signal.isInTrunk() || (signal.isInTrunk() && ((signal.numSteps + treeHash) % 5 == 0) && (radius > 1) ) ? 2 : 0;//Only allow turns when we aren't in the trunk(or the branch is not a twig)
			
			int height = 18 + ((treeHash % 7829) % 8);
			
			if(signal.delta.getY() < height ) {
				probMap[2 + (posHash % 4)] = sideTurn;
			} else {
				probMap[1] = probMap[2] = probMap[3] = probMap[4] = probMap[5] = 2;//At top of tree allow any direction
			}
			
			probMap[originDir.ordinal()] = 0;//Disable the direction we came from
			probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction 
			
			return probMap;
		}
		
		@Override
		protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
			if(signal.isInTrunk() && newDir != EnumFacing.UP) {//Turned out of trunk
				signal.energy = 4.0f;
			}
			return newDir;
		}
		
		//Jungle trees grow taller in suitable biomes
		@Override
		public float getEnergy(World world, BlockPos pos) {
			return super.getEnergy(world, pos) * biomeSuitability(world, pos);
		}
		
		@Override
		public void postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, boolean worldGen) {
			super.postGeneration(world, rootPos, biome, radius, endPoints, worldGen);

			if(world.rand.nextInt() % 8 == 0) {
				addCocoa(world, rootPos, true);
			}

			if(worldGen) {
				BlockPos treePos = rootPos.up();
				
				//Generate Vines
				vineGen.setQuantity(endPoints.size()).setMaxLength(20).gen(world, treePos, endPoints);

				//Generate undergrowth
				underGen.setRadius(radius).gen(world, treePos, endPoints);
			}
		}
		
		@Override
		public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife, boolean rapid) {
			super.postGrow(world, rootPos, treePos, soilLife, rapid);
			
			if(soilLife == 0 && world.rand.nextInt() % 16 == 0) {
				addCocoa(world, rootPos, false);
			}
			
			return true;
		}

		private void addCocoa(World world, BlockPos rootPos, boolean worldGen) {
			TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new NodeFruitCocoa().setWorldGen(worldGen)));
		}
		
	}
	
	public TreeJungle() {
		super(BlockPlanks.EnumType.JUNGLE);
		ModBlocks.jungleLeavesProperties.setTree(this);
		canSupportCocoa = true;
		
		addConnectableVanillaLeaves(new IConnectable() {
			@Override
			public boolean isConnectable(IBlockState blockState) {
				return blockState.getBlock() instanceof BlockOldLeaf && (blockState.getValue(BlockOldLeaf.VARIANT) == BlockPlanks.EnumType.JUNGLE);
			}
		});
	}

	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesJungle(this));
	}
	
	@Override
	public boolean onTreeActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
	
		//Place Cocoa Pod if we are holding Cocoa Beans
		if(heldItem != null) {
			if(heldItem.getItem() == Items.DYE && heldItem.getItemDamage() == 3) {
				BlockBranch branch = TreeHelper.getBranch(state);
				if(branch != null && branch.getRadius(state, world, pos) == 8) {
					if(side != EnumFacing.UP && side != EnumFacing.DOWN) {
						pos = pos.offset(side);
					}
					if (world.isAirBlock(pos)) {
						IBlockState cocoaState = ModBlocks.blockFruitCocoa.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, 0, player);
						EnumFacing facing = cocoaState.getValue(BlockHorizontal.FACING);
						world.setBlockState(pos, ModBlocks.blockFruitCocoa.getDefaultState().withProperty(BlockHorizontal.FACING, facing), 2);
						if (!player.capabilities.isCreativeMode) {
							CompatHelper.shrinkStack(heldItem, 1);
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
