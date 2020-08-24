package com.ferreusveritas.dynamictrees.api;

//import com.ferreusveritas.dynamictrees.DynamicTrees;
//import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
//import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
//import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
//import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
//import com.ferreusveritas.dynamictrees.blocks.LeavesProperties;
//import com.ferreusveritas.dynamictrees.items.Seed;
//import com.ferreusveritas.dynamictrees.trees.Species;
//import com.ferreusveritas.dynamictrees.trees.TreeFamily;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Blocks;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.registries.IForgeRegistry;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * A {@link TreeFamily} builder class to ease in the creation of new trees for other mods.
// *
// *
// * @author ferreusveritas
// *
// */
//public class TreeBuilder {
//
//	private ResourceLocation name;
//	private int seqNum = -1;
//
//	//Drops
//	private BlockState primitiveLeavesBlockState = Blocks.OAK_LEAVES.getDefaultState();
//	private BlockState primitiveLogBlockState = Blocks.OAK_LOG.getDefaultState();
//	private ItemStack stickItemStack;
//
//	//Leaves
//	private ILeavesProperties dynamicLeavesProperties;
//	private int dynamicLeavesSmotherMax = 4;
//	private int dynamicLeavesLightRequirement = 13;
//	private ResourceLocation dynamicLeavesCellKit;
//
//	//Common Species
//	private ISpeciesCreator speciesCreator;
//	private boolean speciesCreateSeed = true;
//
//	//Extra Species
//	private List<ISpeciesCreator> extraSpeciesCreators = new ArrayList<>(0);
//	private Map<String, Species> extraSpecies = new HashMap<>();
//
//	/**
//	 * Name your tree and give me that name.
//	 *
//	 * REQUIRED
//	 *
//	 * Each tree is given a unique name per mod.  We use a {@link ResourceLocation} to get the
//	 * job done as it contains the ModId and the Name of the object.
//	 *
//	 * @param name A unique {@link ResourceLocation} giving the tree a simple name.
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setName(ResourceLocation name) {
//		this.name = name;
//		return this;
//	}
//
//	/**
//	 * Convenience function for {@link #setName(ResourceLocation) }
//	 * @param domain The domain of your mod e.g. the ModId
//	 * @param path The unique name of this resource
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setName(String domain, String path) {
//		return setName(new ResourceLocation(domain, path));
//	}
//
//	/**
//	 * Serially number your trees and give me that number.
//	 *
//	 * REQUIRED(if not using mutually exclusive member setDynamicLeaves(...))
//	 *
//	 * Each {@link BlockDynamicLeaves} can handle 4 different trees by using metadata.  It's the mod authors
//	 * responsibility to assign and maintain an ordered set of numbers that each represent a tree.  The sequence
//	 * should start from 0 for each mod and incremented for each {@link TreeFamily} that the mod creates.  Gaps in
//	 * the numbered list are okay(if a tree is removed for instance), duplicates will result in undefined behavior.
//	 * DynamicTrees internally maintains a mapping of {@link BlockDynamicLeaves} for each mod.  This is done to
//	 * reduce the number of registered blocks.
//	 *
//	 * @param seqNum The registration sequence number for this MODID. Used for registering 4 leaves types per {@link BlockDynamicLeaves}.
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setDynamicLeavesSequence(int seqNum) {
//		if(dynamicLeavesProperties == null) {
//			this.seqNum = seqNum;
//		}
//		return this;
//	}
//
//	/**
//	 * Set a custom {@link BlockDynamicLeaves}
//	 *
//	 * REQUIRED(if not using mutually exclusive member setDynamicLeavesSequence(...))
//	 *
//	 * @param leavesProperties The {@link BlockDynamicLeaves} to set for this tree
//	 * this.seqNum The number used to select the correct subblock of the {@link BlockDynamicLeaves} (typically 0-3 for a standard {@link BlockDynamicLeaves})
//	 * @return
//	 */
//	public TreeBuilder setDynamicLeavesProperties(ILeavesProperties leavesProperties) {
//		this.seqNum = -1;
//		dynamicLeavesProperties = leavesProperties;
//		return this;
//	}
//
//	/**
//	 * RECOMMENDED
//	 *
//  	 * If not defined the default leaves will be plain Vanilla oak leaves and that's what will drop when sheared.
//	 *
//	 * @param primLeaves The primitive leaves which are used for many purposes including drops, branch reinforcing and some other basic behavior.
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setPrimitiveLeaves(BlockState primLeaves) {
//		primitiveLeavesBlockState = primLeaves;
//		return this;
//	}
//
//	/**
// 	 * RECOMMENDED
// 	 *
// 	 * If not defined the default log will be plain Vanilla oak log and that's what will drop when harvested.
//	 *
//	 * @param primLog The primitive(vanilla) log to base the drops, and other behavior from
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setPrimitiveLog(BlockState primLog) {
//		primitiveLogBlockState = primLog;
//		return this;
//	}
//
//	/**
//	 * OPTIONAL
//	 *
//	 * Generally this is not used.  Some mods have custom sticks like iron sticks and this is for them.
//	 *
//	 * @param stick The sticks to drop when there's not enough harvested material to produce another whole log.
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setStick(ItemStack stick) {
//		this.stickItemStack = stick;
//		return this;
//	}
//
//	/**
//	 * OPTIONAL
//	 *
//	 * @param smotherMax The maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4]
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setSmotherLeavesMax(int smotherMax) {
//		dynamicLeavesSmotherMax = smotherMax;
//		return this;
//	}
//
//	/**
//	 * OPTIONAL
//	 *
//	 * @param light The minimum amount of light necessary for a leaves block to be created.
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setLightRequirement(int light) {
//		dynamicLeavesLightRequirement = light;
//		return this;
//	}
//
//
//	/**
//	 * OPTIONAL
//	 *
//	 * Sets the cellular automata kit to use for the {@link BlockDynamicLeaves} for this
//	 * {@link TreeFamily}
//	 *
//	 * A CellKit contains all the algorithms and data to make a leaves grow a certain way.
//	 *
//	 * @param kit The Cell Kit to use.
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setCellKit(ResourceLocation kit) {
//		dynamicLeavesCellKit = kit;
//		return this;
//	}
//
//	/**
//	 * OPTIONAL
//	 *
//	 * Provides a way to inject a custom common species.  If this is not used
//	 * a default Species will be created for you.
//	 *
//	 * @param speciesCreator
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setCommonSpecies(ISpeciesCreator speciesCreator) {
//		this.speciesCreator = speciesCreator;
//		return this;
//	}
//
//	/**
//	 * OPTIONAL
//	 *
//	 * Provides a way to add extra custom species.
//	 *
//	 * @param speciesCreator
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder addExtraSpecies(ISpeciesCreator speciesCreator) {
//		extraSpeciesCreators.add(speciesCreator);
//		return this;
//	}
//
//	/**
//	 * OPTIONAL
//	 *
//	 * Can be used to turn off automatic seed creation.
//	 * When enabled a seed will automatically be created and
//	 * a standard seed dropper will be added to the common
//	 * species.
//	 *
//	 * @param isStandard true to enable automatic seed creation(default), false to disable
//	 * @return TreeBuilder for chaining
//	 */
//	public TreeBuilder setCreateSeed(boolean isStandard) {
//		speciesCreateSeed = isStandard;
//		return this;
//	}
//
//	/**
//	 * Builds a {@link TreeFamily} according to the specs provided. Called last in the builder chain.
//	 * Repeated calls can be made but be sure to change the Name and Sequence for the tree before
//	 * creating multiple trees.
//	 *
//	 * @return The newly built {@link TreeFamily}
//	 */
//	public TreeFamily build() {
//
//		if(name == null) {
//			System.err.println("Error: Attempted to build an nameless tree");
//			return TreeFamily.NULLFAMILY;
//		}
//
//		if(seqNum == -1 && dynamicLeavesProperties == null) {
//			System.err.println("Error: Attempted to build an unsequenced tree(or a tree without dynamic leaves properties)");
//			return TreeFamily.NULLFAMILY;
//		}
//
//		return new TreeFamily(name) {
//
//			{
//
//				if(dynamicLeavesProperties == null) {
//
//					dynamicLeavesProperties = new LeavesProperties(primitiveLeavesBlockState) {
//						@Override
//						public int getLightRequirement() {
//							return dynamicLeavesLightRequirement;
//						}
//
//						public int getSmotherLeavesMax() {
//							return dynamicLeavesSmotherMax;
//						};
//
//						@Override
//						public ICellKit getCellKit() {
//							return TreeRegistry.findCellKit(dynamicLeavesCellKit);
//						}
//					};
//
//					LeavesPaging.getLeavesBlockForSequence(DynamicTrees.MODID, seqNum, dynamicLeavesProperties, dynamicLeavesProperties.getTree().getName().getPath());
//				}
//
//				this.setPrimitiveLog(primitiveLogBlockState, new ItemStack(primitiveLeavesBlockState.getBlock()));
//
//				dynamicLeavesProperties.setTree(this);
//
//				if(stickItemStack != null) {
//					setStick(stickItemStack);
//				}
//
//			}
//
//			@Override
//			public void createSpecies() {
//
//				setCommonSpecies(speciesCreator != null ? speciesCreator.create(this) : new Species(name, this, dynamicLeavesProperties));
//
//				if(speciesCreateSeed) {
//					getCommonSpecies().generateSeed();
//					getCommonSpecies().setupStandardSeedDropping();
//				}
//
//				for(ISpeciesCreator creator: extraSpeciesCreators) {
//					Species species = creator.create(this);
//					extraSpecies.put(species.getRegistryName().getPath(), species);
//				}
//
//			}
//
//			@Override
//			public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
//				super.registerSpecies(speciesRegistry);
//				extraSpecies.values().forEach(s -> speciesRegistry.register(s));
//			}
//
//			@Override
//			public List<Item> getRegisterableItems(List<Item> itemList) {
//				for(Species species: extraSpecies.values()) {
//					Seed seed = species.getSeed();//Since we generated the species internally we need to let the seed out to be registered.
//					if(seed != Seed.NULLSEED) {
//						itemList.add(seed);
//					}
//				}
//				return super.getRegisterableItems(itemList);
//			}
//		};
//	}
//
//	public interface ISpeciesCreator {
//		Species create(TreeFamily tree);
//	}
//
//}
