package com.ferreusveritas.dynamictrees.api;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.cells.ICellSolver;
import com.ferreusveritas.dynamictrees.api.treedata.IFoliageColorHandler;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A {@link DynamicTree} builder class to ease in the creation of new trees for other mods.
 * 
 * 
 * @author ferreusveritas
 *
 */
public class TreeBuilder {

	private ResourceLocation name;
	private int seqNum = -1;
	
	//Drops
	private IBlockState primitiveLeavesBlockState = Blocks.LEAVES.getDefaultState();
	private ItemStack primitiveLeavesItemStack = new ItemStack(Blocks.LEAVES);
	private IBlockState primitiveLogBlockState = Blocks.LOG.getDefaultState();
	private ItemStack primitiveLogItemStack = new ItemStack(Blocks.LOG);
	private IBlockState primitiveSaplingBlockState;
	private ItemStack primitiveSaplingItemStack;
	private ItemStack stickItemStack;

	//Leaves
	private BlockDynamicLeaves dynamicLeavesBlock;
	private int dynamicLeavesSubBlockNum = 0;
	private int dynamicLeavesSmotherMax = -1;
	private int dynamicLeavesLightRequirement = -1;
	private int dynamicLeavesDefaultHydration = -1;
	private SimpleVoxmap dynamicLeavesClusterVoxmap;
	private ICellSolver dynamicLeavesCellSolver;
	private IFoliageColorHandler dynamicLeavesColorHandler;

	//Common Species
	private IBlockState speciesSaplingBlockState;
	private Block speciesSaplingBlock;
	private boolean speciesCreateSeed = true;
	private boolean speciesCreateSapling = true;
	
	/**
	 * Name your tree and give me that name.
	 * 
	 * REQUIRED
	 * 
	 * Each tree is given a unique name per mod.  We use a {@link ResourceLocation} to get the
	 * job done as it contains the ModId and the Name of the object.
	 * 
	 * @param name A unique {@link ResourceLocation} giving the tree a simple name.
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setName(ResourceLocation name) {
		this.name = name;
		return this;
	}
	
	//Convenience function for the above member
	public TreeBuilder setName(String domain, String path) {
		return setName(new ResourceLocation(domain, path));
	}
	
	/**
	 * Serially number your trees and give me that number.
	 * 
	 * REQUIRED(if not using mutually exclusive member setDynamicLeaves(...))
	 *  
	 * Each {@link BlockDynamicLeaves} can handle 4 different trees by using metadata.  It's the mod authors
	 * responsibility to assign and maintain an ordered set of numbers that each represent a tree.  The sequence
	 * should start from 0 for each Mod and incremented for each {@link DynamicTree} that the mod creates.  Gaps in
	 * the numbered list are okay(if a tree is removed for instance), duplicates will result in undefined behavior.
	 * DynamicTrees internally maintains a mapping of {@link BlockDynamicLeaves} for each mod.  This is done to
	 * reduce the number of registered blocks.
	 * 
	 * @param seqNum The registration sequence number for this MODID. Used for registering 4 leaves types per {@link BlockDynamicLeaves}.
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setDynamicLeavesSequence(int seqNum) {
		if(dynamicLeavesBlock == null) {
			this.seqNum = seqNum;
		}
		return this;
	}

	/**
	 * Set a custom {@link BlockDynamicLeaves}
	 * 
	 * REQUIRED(if not using mutually exclusive member setDynamicLeavesSequence(...))
	 * 
	 * @param leaves The {@link BlockDynamicLeaves} to set for this tree
	 * @param subBlockNum The number used to select the correct subblock of the {@link BlockDynamicLeaves} (typically 0-3 for a standard {@link BlockDynamicLeaves})
	 * @return
	 */
	public TreeBuilder setDynamicLeaves(BlockDynamicLeaves leaves, int subBlockNum) {
		this.seqNum = -1;
		dynamicLeavesBlock = leaves;
		dynamicLeavesSubBlockNum = subBlockNum;
		return this;
	}

	
	/**
	 * RECOMMENDED
	 * 
  	 * If not defined the default leaves will be plain Vanilla oak leaves and that's what will drop when sheared.
	 * 
	 * @param primLeaves The primitive leaves which are used for many purposes including drops, branch reinforcing and some other basic behavior.
	 * @param primLeavesStack cached {@link ItemStack} of primitive leaves(what is returned when leaves are sheared)
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setPrimitiveLeaves(IBlockState primLeaves, ItemStack primLeavesStack) {
		primitiveLeavesBlockState = primLeaves;
		primitiveLeavesItemStack = primLeavesStack;
		return this;
	}

	/**
 	 * RECOMMENDED
 	 * 
 	 * If not defined the default log will be plain Vanilla oak log and that's what will drop when harvested.
	 * 
	 * @param primLog The primitive(vanilla) log to base the drops, and other behavior from
	 * @param primLogStack The cached ItemStack of primitive logs(what is returned when wood is harvested)
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setPrimitiveLog(IBlockState primLog, ItemStack primLogStack) {
		primitiveLogBlockState = primLog;
		primitiveLogItemStack = primLogStack;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * This is simply a way to store a reference to a primitive sapling in the tree for
	 * convenience.  Could be used for recipes or other logic like for a bonsai pot. 
	 * 
	 * @param primSapling
	 * @param primSaplingStack
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setPrimitiveSapling(IBlockState primSapling, ItemStack primSaplingStack) {
		primitiveSaplingBlockState = primSapling;
		primitiveSaplingItemStack = primSaplingStack;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * Generally this is not used.  Some mods have custom sticks like iron sticks and this is for them.
	 * 
	 * @param stick The sticks to drop when there's not enough harvested material to produce another whole log.
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setStick(ItemStack stick) {
		this.stickItemStack = stick;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * @param smotherMax The maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4]
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setSmotherLeavesMax(int smotherMax) {
		dynamicLeavesSmotherMax = smotherMax;
		return this;
	}

	/**
	 * OPTIONAL
	 * 
	 * @param light The minimum amount of light necessary for a leaves block to be created.
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setLightRequirement(int light) {
		dynamicLeavesLightRequirement = light;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * @param leavesHydration The default hydration level of a newly created leaf block [default = 4]
	 * @return TreeBuilder for chaining
	 **/
	public TreeBuilder setDefaultHydration(int leavesHydration) {
		dynamicLeavesDefaultHydration = leavesHydration;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * Recommended to leave as default unless you know what you're doing.
	 * 
	 * A voxel map of leaves blocks that are "stamped" on to the tree during generation.
	 * Note that this does not affect the pattern of tree growth and is only used for generation.
	 * For best results with a custom solutions provide a leaf cluster pattern that best fits the result of what the
	 * cellSolver for this tree produces.
	 */
	public TreeBuilder setLeafCluster(SimpleVoxmap voxmap) {
		dynamicLeavesClusterVoxmap = voxmap;
		return this;
	}

	/** 
	 * OPTIONAL
	 * 
 	 * Recommended to leave as default unless you know what you're doing.
 	 * 
	 * The solver used to calculate the leaves hydration value from the values pulled from adjacent cells [default = deciduous]
	 * If you don't know what this is then leave this alone.
	 */
	public TreeBuilder setCellSolver(ICellSolver solver) {
		dynamicLeavesCellSolver = solver;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * It's up to the mod author to provide an interface that respects Client {@link SideOnly} function 
	 * calls.  For simple colors and mapping it's typically not a problem.  The interface provided 
	 * by the handler is only ever called client side.
	 * 
	 * @param handler
	 * @return
	 */
	public TreeBuilder setColorHandler(IFoliageColorHandler handler) {
		dynamicLeavesColorHandler = handler;
		return this;
	}
	

	/**
	 * OPTIONAL
	 * 
	 * @param state A blockState that will turn into the common species of this tree
	 * @return
	 */
	public TreeBuilder setCommonSpeciesDynamicSapling(IBlockState state) {
		speciesCreateSapling = false;
		speciesSaplingBlockState = state;
		return this;
	}
	
	public TreeBuilder setCreateSeed(boolean isStandard) {
		speciesCreateSeed = isStandard;
		return this;
	}
	
	public TreeBuilder setCreateSapling(boolean doCreate) {
		speciesCreateSapling = doCreate && speciesSaplingBlockState == null;
		return this;
	}
	
	/**
	 * Builds a {@link DynamicTree} according to the specs provided. Called last in the builder chain.
	 * Repeated calls can be made but be sure to change the Name and Sequence for the tree before
	 * creating multiple trees. 
	 * 
	 * @return The newly built {@link DynamicTree}
	 */
	public DynamicTree build() {
	
		if(name == null) {
			System.err.println("Error: Attempted to build an nameless tree");
			return null;
		}
		
		if(seqNum == -1 && dynamicLeavesBlock == null) {
			System.err.println("Error: Attempted to build an unsequenced tree(or a tree without dynamic leaves)");
			return null;
		}
		
		DynamicTree tree = new DynamicTree(name, seqNum) {
						
			{
				
				if(dynamicLeavesBlock != null) {
					setDynamicLeaves(dynamicLeavesBlock, dynamicLeavesSubBlockNum);
				}
				
				setPrimitiveLeaves(primitiveLeavesBlockState, primitiveLeavesItemStack);
				setPrimitiveLog(primitiveLogBlockState, primitiveLogItemStack);
				
				if(primitiveSaplingBlockState != null && primitiveSaplingItemStack != null) {
					setPrimitiveSapling(primitiveSaplingBlockState, primitiveSaplingItemStack);
				}
				
				if(stickItemStack != null) {
					setStick(stickItemStack);
				}
				
				if(dynamicLeavesSmotherMax != -1) {
					setSmotherLeavesMax(dynamicLeavesSmotherMax);
				}

				if(dynamicLeavesLightRequirement != -1) {
					this.lightRequirement = dynamicLeavesLightRequirement;
				}
				
				if(dynamicLeavesDefaultHydration != -1) {
					this.defaultHydration = (byte) dynamicLeavesDefaultHydration;
				}
				
				if(dynamicLeavesClusterVoxmap != null) {
					setLeafCluster(dynamicLeavesClusterVoxmap);
				}
				
				if(dynamicLeavesCellSolver != null) {
					setCellSolver(dynamicLeavesCellSolver);
				}
			
			}
			
			@Override
			public void createSpecies() {
				setCommonSpecies(new Species(name, this));
				
				if(speciesCreateSeed) {
					commonSpecies.generateSeed();
					commonSpecies.setupStandardSeedDropping();
				}
				
				if(speciesCreateSapling) {
					speciesSaplingBlock = new BlockDynamicSapling(name.getResourcePath() + "sapling");
					speciesSaplingBlockState = speciesSaplingBlock.getDefaultState();
				}
				
				if(speciesSaplingBlockState != null) {
					commonSpecies.setDynamicSapling(speciesSaplingBlockState);
				}
			}

			@Override
			public Species getCommonSpecies() {
				return commonSpecies;
			}
			
			@Override
			public List<Block> getRegisterableBlocks(List<Block> blockList) {
				if(speciesCreateSapling) {
					blockList.add(speciesSaplingBlock);
				}
				return super.getRegisterableBlocks(blockList);
			}
			
			@SideOnly(Side.CLIENT)
			@Override
			public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
				if(dynamicLeavesColorHandler != null) {
					return dynamicLeavesColorHandler.foliageColorMultiplier(state, world, pos);
				} else {
					return super.foliageColorMultiplier(state, world, pos);
				}
			}
			
		};
		
		return tree;
	}
	
}
