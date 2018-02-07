package com.ferreusveritas.dynamictrees.trees;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchCactus;
import com.ferreusveritas.dynamictrees.blocks.BlockCactusSapling;
import com.ferreusveritas.dynamictrees.entities.EntityLingeringEffector;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceTransform;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.TreeCodeStore;

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
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

public class TreeCactus extends DynamicTree {
	
	// TODO: Seeds, sapling, remove vanilla cactus world generation, add more JoCodes to the file, tweak growth pattern
	// This is still WIP, just committing it now to sync up with other changes.
	public class SpeciesCactus extends Species {
		
		public SpeciesCactus(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.cactusLeavesProperties);
			
			setBasicGrowingParameters(0.875f, 4.0f, 4, 2, 1.5f);
			
			this.setSoilLongevity(1); // Doesn't live very long
			
			envFactor(Type.SNOWY, 0.25f);
			envFactor(Type.COLD, 0.5f);
			envFactor(Type.SANDY, 1.05f);
			
			addAcceptableSoil(Blocks.SAND, Blocks.HARDENED_CLAY); //TODO: remove dirt and grass and add rooty sand or something
			
			// TODO: Clean up this mess (maybe make JoCode use an interface like ITreeGenerator for easier custom tree generation)
			joCodeStore = new TreeCodeStore(this) {
				@Override
				public void addCode(Species species, int radius, String code) {
					JoCode joCode = new JoCode(code) {
						@Override
						public void generate(World world, Species species, BlockPos rootPos, Biome biome, EnumFacing facing, int radius) {
							IBlockState initialState = world.getBlockState(rootPos); // Save the initial state of the dirt in case this fails
							species.placeRootyDirtBlock(world, rootPos, 0); // Set to unfertilized rooty dirt

							// A Tree generation boundary radius is at least 2 and at most 8
							radius = MathHelper.clamp(radius, 2, 8);
							BlockPos treePos = rootPos.up();
							
							// Create tree
							setFacing(facing);
							generateFork(world, species, 0, rootPos, false);

							// Fix branch thicknesses and map out leaf locations
							BlockBranch branch = TreeHelper.getBranch(world, treePos);
							if(branch != null) {//If a branch exists then the growth was successful
								NodeFindEnds endFinder = new NodeFindEnds(); // This is responsible for gathering a list of branch end points
								MapSignal signal = new MapSignal(endFinder);
								branch.analyse(world, treePos, EnumFacing.DOWN, signal);
								List<BlockPos> endPoints = endFinder.getEnds();
								
								// Allow for special decorations by the tree itself
								species.postGeneration(world, rootPos, biome, radius, endPoints, !careful);
							} else { // The growth failed.. turn the soil back to what it was
								world.setBlockState(rootPos, initialState, careful ? 3 : 2);
							}
						}
						
						@Override
						protected int generateFork(World world, Species species, int codePos, BlockPos pos, boolean disabled) {
							IBlockState defaultBranchState = species.getTree().getDynamicBranch().getDefaultState();
							if (!(defaultBranchState.getBlock() instanceof BlockBranchCactus)) {
								return codePos;
							}
							while (codePos < instructions.size()) {
								int code = getCode(codePos);
								if (code == forkCode) {
									codePos = generateFork(world, species, codePos + 1, pos, disabled);
								} else if(code == returnCode) {
									return codePos + 1;
								} else {
									EnumFacing dir = EnumFacing.getFront(code);
									pos = pos.offset(dir);
									if (!disabled) {
										if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
											boolean trunk = false;
											if (dir == EnumFacing.UP) {
												IBlockState downState = world.getBlockState(pos.down());
												if (TreeHelper.isRooty(downState) || (downState.getBlock() == defaultBranchState.getBlock() && downState.getValue(BlockBranchCactus.TRUNK) && downState.getValue(BlockBranchCactus.ORIGIN) == EnumFacing.DOWN)) {
													trunk = true;
												}
											}
											world.setBlockState(pos, defaultBranchState.withProperty(BlockBranchCactus.TRUNK, trunk).withProperty(BlockBranchCactus.ORIGIN, dir.getOpposite()), careful ? 3 : 2);
										} else {
											disabled = true;
										}
									}
									codePos++;
								}
							}

							return codePos;
						}
					}.setCareful(false);
					getListForRadius(radius).add(joCode);
				}	
			};
			addJoCodes();
			
		}
		
		@Override
		public float getEnergy(World world, BlockPos pos) {
			long day = world.getTotalWorldTime() / 24000L;
			int month = (int)day / 30; //Change the hashs every in-game month
			
			return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (coordHashCode(pos.up(month)) % 3);//Vary the height energy by a psuedorandom hash function
		}
		
		public int coordHashCode(BlockPos pos) {
			int hash = (pos.getX() * 9973 ^ pos.getY() * 8287 ^ pos.getZ() * 9721) >> 1;
			return hash & 0xFFFF;
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
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.MUTATED_DESERT);
		}
		
		@Override
		public boolean handleRot(World world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, boolean rapid) {
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
			
			//System.out.println(signal.energy);
			//System.out.println(probMap);
			
			return probMap;
		}
		
		@Override
		protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
			if(signal.isInTrunk() && newDir != EnumFacing.UP){ //Turned out of trunk
				signal.energy += 0.0f;
			}
			return newDir;
		}
		
	}
	
	public TreeCactus() {
		super(new ResourceLocation(ModConstants.MODID, "cactus"));
		
		IBlockState primCactus = Blocks.CACTUS.getDefaultState();
		
		setPrimitiveLog(primCactus, new ItemStack(Blocks.CACTUS));
		setStick(ItemStack.EMPTY);
		
		setDynamicBranch(new BlockBranchCactus("cactusbranch"));
		
		getCommonSpecies().setDynamicSapling(new BlockCactusSapling("cactussapling").getDefaultState());
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesCactus(this));
		getCommonSpecies().generateSeed();
	}
	
	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
	}
	
	@Override
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		blockList.add(getCommonSpecies().getDynamicSapling().getBlock());
		return super.getRegisterableBlocks(blockList);
	}
	
	// This prevents a crash when recipes are generated to convert seeds to saplings and vice versa
	@Override
	public IBlockState getPrimitiveSaplingBlockState() {
		return null;
	}
	
	// Prevent transformation potions from being used on Cacti
	@Override
	public boolean applySubstance(World world, BlockPos rootPos, BlockPos hitPos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
		ISubstanceEffect effect = getSubstanceEffect(itemStack);
		
		if (effect != null && !(effect instanceof SubstanceTransform)) {
			if(effect.isLingering()) {
				CompatHelper.spawnEntity(world, new EntityLingeringEffector(world, rootPos, effect));
				return true;
			} else {
				return effect.apply(world, rootPos);
			}
		}
		
		return false;
	}
	
}
