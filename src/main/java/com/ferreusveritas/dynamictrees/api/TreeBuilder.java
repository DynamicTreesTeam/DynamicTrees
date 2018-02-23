package com.ferreusveritas.dynamictrees.api;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.LeavesProperties;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
	private IBlockState primitiveLogBlockState = Blocks.LOG.getDefaultState();
	private ItemStack primitiveSaplingItemStack;
	private ItemStack stickItemStack;

	//Leaves
	private ILeavesProperties dynamicLeavesProperties;
	private int dynamicLeavesSmotherMax = 4;
	private int dynamicLeavesLightRequirement = 13;
	private ResourceLocation dynamicLeavesCellKit;

	//Common Species
	private ISpeciesCreator speciesCreator;
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
		if(dynamicLeavesProperties == null) {
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
	public TreeBuilder setDynamicLeavesProperties(ILeavesProperties leavesProperties) {
		this.seqNum = -1;
		dynamicLeavesProperties = leavesProperties;
		return this;
	}

	
	/**
	 * RECOMMENDED
	 * 
  	 * If not defined the default leaves will be plain Vanilla oak leaves and that's what will drop when sheared.
	 * 
	 * @param primLeaves The primitive leaves which are used for many purposes including drops, branch reinforcing and some other basic behavior.
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setPrimitiveLeaves(IBlockState primLeaves) {
		primitiveLeavesBlockState = primLeaves;
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
	public TreeBuilder setPrimitiveLog(IBlockState primLog) {
		primitiveLogBlockState = primLog;
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
	public TreeBuilder setPrimitiveSapling(ItemStack primSaplingStack) {
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
	 * Sets the cellular automata kit to use for the {@link BlockDynamicLeaves} for this 
	 * {@link DynamicTree}
	 * 
	 * A CellKit contains all the algorithms and data to make a leaves grow a certain way.
	 * 
	 * @param kit The Cell Kit to use.
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setCellKit(ResourceLocation kit) {
		dynamicLeavesCellKit = kit; 
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * Provides a way to inject a custom common species.  If this is not used
	 * a default Species will be created for you.
	 * 
	 * @param speciesCreator
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setCommonSpecies(ISpeciesCreator speciesCreator) {
		this.speciesCreator = speciesCreator;
		return this;
	}

	/**
	 * OPTIONAL
	 * 
	 * @param state A blockState that will turn into the common species of this tree
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setCommonSpeciesDynamicSapling(IBlockState state) {
		speciesCreateSapling = false;
		speciesSaplingBlockState = state;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * Can be used to turn off automatic seed creation.
	 * When enabled a seed will automatically be created and
	 * a standard seed dropper will be added to the common 
	 * species.
	 *  
	 * @param isStandard true to enable automatic seed creation(default), false to disable
	 * @return TreeBuilder for chaining
	 */
	public TreeBuilder setCreateSeed(boolean isStandard) {
		speciesCreateSeed = isStandard;
		return this;
	}
	
	/**
	 * OPTIONAL
	 * 
	 * Can be used to turn off automatic dynamic sapling creation.
	 * 
	 * @param doCreate true to enable automatic sapling creation(default), false to disable
	 * @return TreeBuilder for chaining
	 */
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
		
		if(seqNum == -1 && dynamicLeavesProperties == null) {
			System.err.println("Error: Attempted to build an unsequenced tree(or a tree without dynamic leaves properties)");
			return null;
		}
		
		DynamicTree tree = new DynamicTree(name) {
						
			{
				
				if(dynamicLeavesProperties == null) {

					dynamicLeavesProperties = new LeavesProperties(primitiveLeavesBlockState) {
						@Override
						public int getLightRequirement() {
							return dynamicLeavesLightRequirement;
						}

						public int getSmotherLeavesMax() {
							return dynamicLeavesSmotherMax;
						};

						@Override
						public ICellKit getCellKit() {
							return TreeRegistry.findCellKit(dynamicLeavesCellKit);
						}
					};

					TreeHelper.getLeavesBlockForSequence(ModConstants.MODID, seqNum, dynamicLeavesProperties);
				}
				
				this.setPrimitiveLog(primitiveLogBlockState);
				
				dynamicLeavesProperties.setTree(this);
				
				if(primitiveSaplingItemStack != null) {
					setPrimitiveSapling(primitiveSaplingItemStack);
				}
				
				if(stickItemStack != null) {
					setStick(stickItemStack);
				}
			
			}
			
			@Override
			public void createSpecies() {
	
				setCommonSpecies(speciesCreator != null ? speciesCreator.create(this) : new Species(name, this, dynamicLeavesProperties));
				
				if(speciesCreateSeed) {
					getCommonSpecies().generateSeed();
					getCommonSpecies().setupStandardSeedDropping();
				}
				
				if(speciesCreateSapling) {
					speciesSaplingBlock = new BlockDynamicSapling(name.getResourcePath() + "sapling");
					speciesSaplingBlockState = speciesSaplingBlock.getDefaultState();
				}
				
				if(speciesSaplingBlockState != null) {
					getCommonSpecies().setDynamicSapling(speciesSaplingBlockState);
				}
			}
			
			@Override
			public List<Block> getRegisterableBlocks(List<Block> blockList) {
				if(speciesCreateSapling) {
					blockList.add(speciesSaplingBlock);
				}
				return super.getRegisterableBlocks(blockList);
			}
			
		};
		
		return tree;
	}
	
	public interface ISpeciesCreator {
		Species create(DynamicTree tree);
	}
	
}
