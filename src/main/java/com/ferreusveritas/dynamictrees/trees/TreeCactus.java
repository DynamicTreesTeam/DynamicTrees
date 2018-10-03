package com.ferreusveritas.dynamictrees.trees;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchCactus;
import com.ferreusveritas.dynamictrees.blocks.BlockCactusSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.event.SpeciesPostGenerationEvent;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceTransform;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;

public class TreeCactus extends TreeFamily {
	
	public class SpeciesCactus extends Species {
		
		public SpeciesCactus(TreeFamily treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.cactusLeavesProperties);
			
			setBasicGrowingParameters(0.875f, 4.0f, 4, 2, 1.0f);
			
			setDynamicSapling(new BlockCactusSapling("cactussapling").getDefaultState());
			
			this.setSoilLongevity(1);
			
			addDropCreator(new DropCreator(new ResourceLocation(ModConstants.MODID, "cactusseeds")) {
				@Override
				public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int volume) {
					int numLogs = volume / 8192; 
					while(numLogs > 0) {
						dropList.add(species.getSeedStack(numLogs >= 64 ? 64 : numLogs));
						numLogs -= 64;
					}
					return dropList;
				}
			});
			
			envFactor(Type.SNOWY, 0.25f);
			envFactor(Type.COLD, 0.5f);
			envFactor(Type.SANDY, 1.05f);
			
			clearAcceptableSoils();
			addAcceptableSoil(Blocks.SAND);
		}
		
		@Override
		public JoCode getJoCode(String joCodeString) {
			return new JoCodeCactus(joCodeString);
		}
		
		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getTotalWorldTime() / 24000L;
			int month = (int)day / 30; //Change the hashs every in-game month
			
			return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.up(month), 2) % 3);//Vary the height energy by a psuedorandom hash function
		}
		
		@Override
		public BlockRooty getRootyBlock() {
			return ModBlocks.blockRootySand;
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.MUTATED_DESERT);
		}
		
		@Override
		public boolean handleRot(World world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, SafeChunkBounds safeBounds) {
			return false;
		}
		
		@Override
		protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
			EnumFacing originDir = signal.dir.getOpposite();
			
			//Alter probability map for direction change
			probMap[0] = 0;//Down is always disallowed for cactus
			probMap[1] = signal.delta.getX() % 2 == 0 || signal.delta.getZ() % 2 == 0 ? getUpProbability() : 0;
			probMap[2] = probMap[3] = probMap[4] = probMap[5] = signal.isInTrunk() && (signal.energy > 1) ? 1 : 0;
			if (signal.dir != EnumFacing.UP) probMap[signal.dir.ordinal()] = 0;//Disable the current direction, unless that direction is up
			probMap[originDir.ordinal()] = 0;//Disable the direction we came from
			return probMap;
		}
		
		@Override
		protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
			if(signal.isInTrunk() && newDir != EnumFacing.UP){ //Turned out of trunk
				signal.energy += 0.0f;
			}
			return newDir;
		}
		
		@Override
		public boolean applySubstance(World world, BlockPos rootPos, BlockPos hitPos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
			
			// Prevent transformation potions from being used on Cacti
			if(!(getSubstanceEffect(itemStack) instanceof SubstanceTransform)) {
				return super.applySubstance(world, rootPos, hitPos, player, hand, itemStack);
			}
			
			return false;
		}
		
		@Override
		public boolean canBoneMeal() {
			return false;
		}
		
	}
	
	public TreeCactus() {
		super(new ResourceLocation(ModConstants.MODID, "cactus"));
				
		setPrimitiveLog(Blocks.CACTUS.getDefaultState(), new ItemStack(Blocks.CACTUS));
		setStick(ItemStack.EMPTY);
	}
	
	@Override
	public BlockBranch createBranch() {
		return new BlockBranchCactus("cactusbranch");
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
		setCommonSpecies(new SpeciesCactus(this));
	}
	
	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		getCommonSpecies().generateSeed();
	}
	
	@Override
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		blockList.add(getCommonSpecies().getDynamicSapling().getBlock());
		return super.getRegisterableBlocks(blockList);
	}
	
	protected class JoCodeCactus extends JoCode {
		
		public JoCodeCactus(String code) {
			super(code);
		}
		
		@Override
		public void generate(World world, Species species, BlockPos rootPos, Biome biome, EnumFacing facing, int radius, SafeChunkBounds safeBounds) {
			IBlockState initialState = world.getBlockState(rootPos); // Save the initial state of the dirt in case this fails
			species.placeRootyDirtBlock(world, rootPos, 0); // Set to unfertilized rooty dirt
			
			// A Tree generation boundary radius is at least 2 and at most 8
			radius = MathHelper.clamp(radius, 2, 8);
			BlockPos treePos = rootPos.up();
			
			// Create tree
			setFacing(facing);
			generateFork(world, species, 0, rootPos, false);
			
			// Fix branch thicknesses and map out leaf locations
			BlockBranch branch = TreeHelper.getBranch(world.getBlockState(treePos));
			if(branch != null) {//If a branch exists then the growth was successful
				NodeFindEnds endFinder = new NodeFindEnds(); // This is responsible for gathering a list of branch end points
				MapSignal signal = new MapSignal(endFinder);
				branch.analyse(world.getBlockState(treePos), world, treePos, EnumFacing.DOWN, signal);
				List<BlockPos> endPoints = endFinder.getEnds();
				
				// Allow for special decorations by the tree itself
				species.postGeneration(world, rootPos, biome, radius, endPoints, safeBounds);
				MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(world, species, rootPos, endPoints, safeBounds));
			} else { // The growth failed.. turn the soil back to what it was
				world.setBlockState(rootPos, initialState, careful ? 3 : 2);
			}
		}
		
		@Override
		public boolean setBlockForGeneration(World world, Species species, BlockPos pos, EnumFacing dir, boolean careful) {
			IBlockState defaultBranchState = species.getFamily().getDynamicBranch().getDefaultState();
			if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
				boolean trunk = false;
				if (dir == EnumFacing.UP) {
					IBlockState downState = world.getBlockState(pos.down());
					if (TreeHelper.isRooty(downState) || (downState.getBlock() == defaultBranchState.getBlock() && downState.getValue(BlockBranchCactus.TRUNK) && downState.getValue(BlockBranchCactus.ORIGIN) == EnumFacing.DOWN)) {
						trunk = true;
					}
				}
				return !world.setBlockState(pos, defaultBranchState.withProperty(BlockBranchCactus.TRUNK, trunk).withProperty(BlockBranchCactus.ORIGIN, dir.getOpposite()), careful ? 3 : 2);
			}
			return true;
		}
		
	}
	
}
