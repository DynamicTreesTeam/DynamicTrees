package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.CactusBranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.event.SpeciesPostGenerationEvent;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeNetVolume;
import com.ferreusveritas.dynamictrees.systems.substances.TransformSubstance;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.Random;

public class Cactus extends TreeFamily {
	
	public class CactusSpecies extends Species {
		
		public CactusSpecies(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			setBasicGrowingParameters(0.875f, 4.0f, 4, 2, 1.0f);
			
			this.setSoilLongevity(1);

			generateSapling();

			addDropCreator(new DropCreator(new ResourceLocation(DynamicTrees.MOD_ID, "cactusseeds")) {
				@Override
				public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, NodeNetVolume.Volume volume) {
					int numLogs = (int) (volume.getVolume() / 2);
					while(numLogs > 0) {
						dropList.add(species.getSeedStack(Math.min(numLogs, 64)));
						numLogs -= 64;
					}
					return dropList;
				}
			});
			
			envFactor(Type.SNOWY, 0.25f);
			envFactor(Type.COLD, 0.5f);
			envFactor(Type.SANDY, 1.05f);

		}

		@Override
		protected void setStandardSoils() {
			addAcceptableSoils(DirtHelper.SAND_LIKE);
		}

		@Override
		public boolean isTransformable() {
			return false;
		}

		@Override
		public JoCode getJoCode(String joCodeString) {
			return new JoCodeCactus(joCodeString);
		}
		
		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getGameTime() / 24000L;
			int month = (int)day / 30; //Change the hashs every in-game month
			
			return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.up(month), 2) % 3);//Vary the height energy by a psuedorandom hash function
		}

		@Override
		public boolean isBiomePerfect(RegistryKey<Biome> biome) {
			return BiomeDictionary.hasType(biome, Type.DRY) && BiomeDictionary.hasType(biome, Type.SANDY);
		}
		
		@Override
		public boolean handleRot(IWorld world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, SafeChunkBounds safeBounds) {
			return false;
		}
		
		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
			Direction originDir = signal.dir.getOpposite();
			
			//Alter probability map for direction change
			probMap[0] = 0;//Down is always disallowed for cactus
			probMap[1] = signal.delta.getX() % 2 == 0 || signal.delta.getZ() % 2 == 0 ? getUpProbability() : 0;
			probMap[2] = probMap[3] = probMap[4] = probMap[5] = signal.isInTrunk() && (signal.energy > 1) ? 1 : 0;
			if (signal.dir != Direction.UP) probMap[signal.dir.ordinal()] = 0;//Disable the current direction, unless that direction is up
			probMap[originDir.ordinal()] = 0;//Disable the direction we came from
			return probMap;
		}
		
		@Override
		protected Direction newDirectionSelected(Direction newDir, GrowSignal signal) {
			if(signal.isInTrunk() && newDir != Direction.UP){ //Turned out of trunk
				signal.energy += 0.0f;
			}
			return newDir;
		}
		
		@Override
		public boolean applySubstance(World world, BlockPos rootPos, BlockPos hitPos, PlayerEntity player, Hand hand, ItemStack itemStack) {
			
			// Prevent transformation potions from being used on Cacti
			if(!(getSubstanceEffect(itemStack) instanceof TransformSubstance)) {
				return super.applySubstance(world, rootPos, hitPos, player, hand, itemStack);
			}
			
			return false;
		}
		
		@Override
		public boolean canBoneMeal() {
			return false;
		}
		
		@Override
		public boolean transitionToTree(World world, BlockPos pos) {
			//Ensure planting conditions are right
			TreeFamily tree = getFamily();
			if(world.isAirBlock(pos.up()) && isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
				world.setBlockState(pos, tree.getDynamicBranch().getDefaultState());//set to a single branch
				placeRootyDirtBlock(world, pos.down(), 15);//Set to fully fertilized rooty sand underneath
				return true;
			}
			
			return false;
		}

		@Override
		public VoxelShape getSaplingShape() {
			return VoxelShapes.create(new AxisAlignedBB(0.375f, 0.0f, 0.375f, 0.625f, 0.5f, 0.625f));
		}
		
		public SoundType getSaplingSound() {
			return SoundType.CLOTH;
		}
	}
	
	public Cactus() {
		super(new ResourceLocation(DynamicTrees.MOD_ID, "cactus"));
		
		setPrimitiveLog(Blocks.CACTUS);
		setStick(Items.AIR);
	}
	
	@Override
	public ILeavesProperties getCommonLeaves() {
		return new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare"));//Explicitly unbuilt since there's no leaves
	}
	
	@Override
	public BranchBlock createBranch() {
		String branchName = this.getName() + "_branch";
		return new CactusBranchBlock( branchName);
	}

	@Override
	public boolean hasStrippedBranch() {
		return false;
	}

	@Override
	public float getPrimaryThickness() {
		return 5.0f;
	}
	
	@Override
	public float getSecondaryThickness() {
		return 4.0f;
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new CactusSpecies(this));
	}

	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		getCommonSpecies().generateSeed();
	}
	
	protected class JoCodeCactus extends JoCode {
		
		public JoCodeCactus(String code) {
			super(code);
		}
		
		@Override
		public void generate(IWorld world, Species species, BlockPos rootPos, Biome biome, Direction facing, int radius, SafeChunkBounds safeBounds) {
			BlockState initialDirtState = world.getBlockState(rootPos); // Save the initial state of the dirt in case this fails
			species.placeRootyDirtBlock(world, rootPos, 0); // Set to unfertilized rooty dirt
			
			// A Tree generation boundary radius is at least 2 and at most 8
			radius = MathHelper.clamp(radius, 2, 8);
			BlockPos treePos = rootPos.up();
			
			// Create tree
			setFacing(facing);
			generateFork(world, species, 0, rootPos, false);
			
			// Fix branch thicknesses and map out leaf locations
			BranchBlock branch = TreeHelper.getBranch(world.getBlockState(treePos));
			if(branch != null) {//If a branch exists then the growth was successful
				NodeFindEnds endFinder = new NodeFindEnds(); // This is responsible for gathering a list of branch end points
				MapSignal signal = new MapSignal(endFinder);
				branch.analyse(world.getBlockState(treePos), world, treePos, Direction.DOWN, signal);
				List<BlockPos> endPoints = endFinder.getEnds();
				
				// Allow for special decorations by the tree itself
				species.postGeneration(world, rootPos, biome, radius, endPoints, safeBounds, initialDirtState);
				MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(world, species, rootPos, endPoints, safeBounds, initialDirtState));
			} else { // The growth failed.. turn the soil back to what it was
				world.setBlockState(rootPos, initialDirtState, careful ? 3 : 2);
			}
		}
		
		@Override
		public boolean setBlockForGeneration(IWorld world, Species species, BlockPos pos, Direction dir, boolean careful) {
			BlockState defaultBranchState = species.getFamily().getDynamicBranch().getDefaultState();
			if (world.getBlockState(pos).getMaterial().isReplaceable()) {
				boolean trunk = false;
				if (dir == Direction.UP) {
					BlockState downState = world.getBlockState(pos.down());
					if (TreeHelper.isRooty(downState) || (downState.getBlock() == defaultBranchState.getBlock() && downState.get(CactusBranchBlock.TRUNK) && downState.get(CactusBranchBlock.ORIGIN) == Direction.DOWN)) {
						trunk = true;
					}
				}
				return !world.setBlockState(pos, defaultBranchState.with(CactusBranchBlock.TRUNK, trunk).with(CactusBranchBlock.ORIGIN, dir.getOpposite()), careful ? 3 : 2);
			}
			return true;
		}
		
	}
	
}
