package com.ferreusveritas.dynamictrees.trees;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorApple;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenHugeMushrooms;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeDarkOak extends TreeFamilyVanilla {
	
	public class SpeciesDarkOak extends Species {
		
		FeatureGenHugeMushrooms underGen;
		
		SpeciesDarkOak(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.darkOakLeavesProperties);
			
			//Dark Oak Trees are tall, slowly growing, thick trees
			setBasicGrowingParameters(0.30f, 18.0f, 4, 6, 0.8f);
			
			setSoilLongevity(14);//Grows for a long long time
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.25f);
			envFactor(Type.MUSHROOM, 1.25f);
			
			if(ModConfigs.worldGen && !ModConfigs.enableAppleTrees) {
				addDropCreator(new DropCreatorApple());
			}
			
			setupStandardSeedDropping();
			underGen = new FeatureGenHugeMushrooms(this);
		}
		
		@Override
		public boolean preGeneration(World world, BlockPos rootPos, int radius, EnumFacing facing, SafeChunkBounds safeBounds, JoCode joCode, IBlockState initialDirtState) {
			//Erase a volume of blocks that could potentially get in the way
			for(MutableBlockPos pos : BlockPos.getAllInBoxMutable(rootPos.add(new Vec3i(-1,  1, -1)), rootPos.add(new Vec3i(1, 6, 1)))) {
				world.setBlockToAir(pos);
			}
			return true;
		}
		
		@Override
		public void postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
			super.postGeneration(world, rootPos, biome, radius, endPoints, safeBounds, initialDirtState);

			int trunkRadius = flareBottom(world, rootPos);
			
			if(safeBounds != SafeChunkBounds.ANY) {//worldgen
				
				BlockPos treePos = rootPos.up();
				
				//Place dirt blocks around rooty dirt block if tree has a > 8 radius
				IBlockState branchState = world.getBlockState(treePos);
				if(TreeHelper.getTreePart(branchState).getRadius(branchState) > BlockBranch.RADMAX_NORMAL) {
					for(Surround dir: Surround.values()) {
						world.setBlockState(rootPos.add(dir.getOffset()), initialDirtState);
					}
				}
								
				//Generate huge mushroom undergrowth
				underGen.setRadius(radius).gen(world, treePos, endPoints, safeBounds);
			}

			generateRoots(world, rootPos, trunkRadius, safeBounds);
			
		}
		
		/**
		 * Put a cute little flare on the bottom of the dark oaks
		 * 
		 * @param world The world
		 * @param rootPos The position of the rooty dirt block of the tree
		 * @return The radius of the bottom trunk section after operation
		 */
		public int flareBottom(World world, BlockPos rootPos) {
			
			//Put a cute little flare on the bottom of the dark oaks
			int radius3 = TreeHelper.getRadius(world, rootPos.up(3));
			
			if(radius3 > 6) {
				getDynamicBranch().setRadius(world, rootPos.up(2), radius3 + 1, EnumFacing.UP);
				return getDynamicBranch().setRadius(world, rootPos.up(1), radius3 + 2, EnumFacing.UP);
			}
			
			return TreeHelper.getRadius(world, rootPos.up(1));
		}
		
		protected int getRootRadius(int trunkRadius) {
			if(trunkRadius > 13) {
				switch(trunkRadius) {
					case 14: return 3;
					case 15: return 4;
					case 16: return 5;
					case 17: return 6;
					default: return 8;
				}
			}
			return 0;
		}
		
		protected void generateRoots(World world, BlockPos rootPos, int trunkRadius, SafeChunkBounds safeBounds) {
			
			SimpleVoxmap rootMap = new SimpleVoxmap(5, 1, 5, new byte[] {
				2, 3, 4, 0, 2,
				0, 0, 5, 0, 3,
				4, 5, 0, 5, 4,
				0, 0, 5, 0, 0,
				2, 3, 4, 0, 0,
			}).setCenter(new BlockPos(2, 0, 2));
			
			nextRoot(world, rootMap, rootPos.up(), BlockPos.ORIGIN, 0, null, 0);
		}
		
		protected void nextRoot(World world, SimpleVoxmap rootMap, BlockPos trunkPos, BlockPos pos, int height, EnumFacing fromDir, int radius) {
			
			for(int i = 0; i < 2; i++) {
				BlockPos currPos = trunkPos.add(pos).up(height - i);
				IBlockState placeState = world.getBlockState(currPos);
				IBlockState belowState = world.getBlockState(currPos.down());
				
				if(pos == BlockPos.ORIGIN || (placeState.getBlock() == ModBlocks.blockTrunkShell || placeState.getBlock().isReplaceable(world, currPos)) && belowState.isNormalCube()) {
					if(radius > 0) {
						getSurfaceRoots().setRadius(world, currPos, radius, fromDir, 3);
					}
					for(EnumFacing dir: EnumFacing.HORIZONTALS) {
						if(dir != fromDir) {
							BlockPos dPos = pos.offset(dir);
							byte rad = rootMap.getVoxel(dPos);
							if(rad != 0) {
								nextRoot(world, rootMap, trunkPos, dPos, height - i, dir.getOpposite(), rad);
							}
						}
					}
					break;
				}
			}
			
		}
		
		protected void generateRootsOld(World world, BlockPos rootPos, int trunkRadius, SafeChunkBounds safeBounds) {
			
			int rootRadius = getRootRadius(trunkRadius);
			BlockPos trunkPos = rootPos.up();
			
			if(rootRadius > 0) {
				around:	for(EnumFacing dir: EnumFacing.HORIZONTALS) {
					BlockPos dirtPos = rootPos.offset(dir);
					int hash = ((dirtPos.hashCode() * 15391027) >> 2);
					if((hash & 1) == 0) {
						BlockPos airPos = dirtPos.up();
						//Check to ensure there's a valid location for the root branch.  Check for an available replaceable block with valid soil below that.
						IBlockState airState = world.getBlockState(airPos);
						Block airBlock = airState.getBlock();
						if( (airBlock == ModBlocks.blockTrunkShell || airBlock.isReplaceable(world, airPos)) && isAcceptableSoil(world, dirtPos, world.getBlockState(dirtPos))) {
							//Check around the block for existing branches(except for the trunk of this tree)
							for(EnumFacing checkDir: EnumFacing.VALUES) {
								BlockPos checkPos = airPos.offset(checkDir);
								if(!checkPos.equals(trunkPos) && TreeHelper.isBranch(world.getBlockState(checkPos)) ) { //Don't count the trunk but look for adjacent branches
									continue around;
								}
							}
							
							world.setBlockState(dirtPos, ModBlocks.blockRootyDirtFake.getDefaultState());
							getDynamicBranch().setRadius(world, airPos, MathHelper.clamp(rootRadius + ((hash & 2) >> 1), 1, 8), dir.getOpposite());
						}
					}
				}
			}
			
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.ROOFED_FOREST);
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
		public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife, boolean natural) {
			int trunkRadius = flareBottom(world, rootPos);
			//generateRoots(world, rootPos, trunkRadius, SafeChunkBounds.ANY);

			return super.postGrow(world, rootPos, treePos, soilLife, natural);
		}
		
		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
			
			probMap[EnumFacing.UP.getIndex()] = 4;
			
			//Disallow up/down turns after having turned out of the trunk once.
			if(!signal.isInTrunk()) {
				probMap[EnumFacing.UP.getIndex()] = 0;
				probMap[EnumFacing.DOWN.getIndex()] = 0;
				probMap[signal.dir.ordinal()] *= 0.35;//Promotes the zag of the horizontal branches
			}
			
			//Amplify cardinal directions to encourage spread the higher we get
			float energyRatio = signal.delta.getY() / getEnergy(world, pos);
			float spreadPush = energyRatio * 2;
			spreadPush = spreadPush < 1.0f ? 1.0f : spreadPush;
			for(EnumFacing dir: EnumFacing.HORIZONTALS) {
				probMap[dir.ordinal()] *= spreadPush;
			}
			
			//Ensure that the branch gets out of the trunk at least two blocks so it won't interfere with new side branches at the same level 
			if(signal.numTurns == 1 && signal.delta.distanceSq(0, signal.delta.getY(), 0) == 1.0 ) {
				for(EnumFacing dir: EnumFacing.HORIZONTALS) {
					if(signal.dir != dir) {
						probMap[dir.ordinal()] = 0;;
					}
				}
			}
			
			//If the side branches are too swole then give some other branches a chance
			if(signal.isInTrunk()) {
				for(EnumFacing dir: EnumFacing.HORIZONTALS) {
					if(probMap[dir.ordinal()] >= 7) {
						probMap[dir.ordinal()] = 2;
					}
				}
				if(signal.delta.getY() > getLowestBranchHeight() + 5) {
					probMap[EnumFacing.UP.ordinal()] = 0;
					signal.energy = 2;
				}
			}
			
			return probMap;
		}
		
		@Override
		public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
			if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
				if(radius > 2 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(EnumSkyBlock.SKY, pos) < 6) {
					world.setBlockState(pos, ModBlocks.blockStates.redMushroom);//Change branch to a red mushroom
					world.setBlockState(pos.down(), ModBlocks.blockStates.podzol);//Change rooty dirt to Podzol
				}
				return true;
			}
			
			return false;
		}
	}
	
	BlockSurfaceRoot surfaceRootBlock;
	
	public TreeDarkOak() {
		super(BlockPlanks.EnumType.DARK_OAK);
		ModBlocks.darkOakLeavesProperties.setTree(this);
		
		surfaceRootBlock = new BlockSurfaceRoot(Material.WOOD, getName() + "root");
		
		addConnectableVanillaLeaves((state) -> { return state.getBlock() instanceof BlockNewLeaf && (state.getValue(BlockNewLeaf.VARIANT) == BlockPlanks.EnumType.DARK_OAK); });
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesDarkOak(this));
	}
	
	@Override
	public boolean isThick() {
		return true;
	}
	
	@Override
	public int getRadiusForCellKit(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, BlockBranch branch) {
		int radius = branch.getRadius(blockState);
		if(radius == 1) {
			if(blockAccess.getBlockState(pos.down()).getBlock() == branch) {
				return 128;
			}
		}
		return radius;
	}
	
	@Override
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		blockList = super.getRegisterableBlocks(blockList);
		blockList.add(surfaceRootBlock);
		return blockList;
	}
	
	@Override
	public List<Item> getRegisterableItems(List<Item> itemList) {
		itemList = super.getRegisterableItems(itemList);
		itemList.add(new ItemBlock(surfaceRootBlock).setRegistryName(surfaceRootBlock.getRegistryName()));
		return itemList;
	}
	
	@Override
	public BlockSurfaceRoot getSurfaceRoots() {
		return surfaceRootBlock;
	}
	
}
