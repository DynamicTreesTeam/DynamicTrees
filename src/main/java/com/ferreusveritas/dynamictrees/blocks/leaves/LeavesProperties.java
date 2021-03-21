package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.cells.CellKits;
import com.ferreusveritas.dynamictrees.client.BlockColorMultipliers;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.trees.IResettable;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

/**
 * This class provides a means of holding individual properties
 * for leaves.  This is necessary since leaves can contain sub blocks
 * that may behave differently.  Each leaves properties object
 * must have a reference to a tree family.
 * 
 * @author ferreusveritas
 */
public class LeavesProperties extends RegistryEntry<LeavesProperties> implements IResettable<LeavesProperties> {
	
	public static final LeavesProperties NULL_PROPERTIES = new LeavesProperties(DTTrees.NULL) {
		@Override public LeavesProperties setFamily(Family family) { return this; }
		@Override public Family getFamily() { return Family.NULL_FAMILY; }
		@Override public BlockState getPrimitiveLeaves() { return Blocks.AIR.getDefaultState(); }
		@Override public ItemStack getPrimitiveLeavesItemStack() { return ItemStack.EMPTY; }
		@Override public LeavesProperties setDynamicLeavesState(BlockState state) { return this; }
		@Override public BlockState getDynamicLeavesState() { return Blocks.AIR.getDefaultState(); }
		@Override public BlockState getDynamicLeavesState(int hydro) { return Blocks.AIR.getDefaultState(); }
		@Override public CellKit getCellKit() { return CellKit.NULL_CELL_KIT; }
		@Override public int getFlammability() { return 0; }
		@Override public int getFireSpreadSpeed() { return 0; }
		@Override public int getSmotherLeavesMax() { return 0; }
		@Override public int getLightRequirement() { return 15; }
		@Override public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) { return false; }
	};

	/**
	 * Central registry for all {@link LeavesProperties} objects.
	 */
	public static final TypedRegistry<LeavesProperties, Type> REGISTRY = new TypedRegistry<>(LeavesProperties.class, NULL_PROPERTIES, new Type());

	public static class Type extends TypedRegistry.EntryType<LeavesProperties> {
		@Override
		public LeavesProperties construct(final ResourceLocation registryName) {
			return new LeavesProperties(registryName);
		}
	}

	protected static final int maxHydro = 4;

	protected ResourceLocation primitiveLeavesRegName;

	/** The primitive (vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	protected BlockState primitiveLeaves;

	protected Family.IConnectable primitiveLeavesConnectable;

	/** The {@link CellKit}, which is for leaves automata. */
	protected CellKit cellKit;

	protected Family family = Family.NULL_FAMILY;
	protected DynamicLeavesBlock dynamicLeavesBlock;
	protected BlockState[] dynamicLeavesBlockHydroStates = new BlockState[maxHydro+1];
	protected int flammability = 60;// Mimic vanilla leaves
	protected int fireSpreadSpeed = 30;// Mimic vanilla leaves

	protected int smotherLeavesMax = 4;
	protected int lightRequirement = 13;
	protected boolean connectAnyRadius = false;

	private LeavesProperties() {}

	public LeavesProperties(final ResourceLocation registryName) {
		this(null, registryName);
	}
	
	public LeavesProperties(@Nullable final BlockState primitiveLeaves, final ResourceLocation registryName) {
		this(primitiveLeaves, CellKits.DECIDUOUS, registryName);
	}
	
	public LeavesProperties(@Nullable final BlockState primitiveLeaves, final CellKit cellKit, final ResourceLocation registryName) {
		this.primitiveLeaves = primitiveLeaves != null ? primitiveLeaves : DTRegistries.BLOCK_STATES.AIR;
		this.cellKit = cellKit;
		this.setRegistryName(registryName);
	}

	/**
	 * Gets the primitive (vanilla) leaves for these {@link LeavesProperties}.
	 *
	 * @return The {@link BlockState} for the primitive leaves.
	 */
	public BlockState getPrimitiveLeaves() {
		return primitiveLeaves;
	}

	public void setPrimitiveLeaves(final Block primitiveLeaves) {
		if (this.primitiveLeaves == null || primitiveLeaves != this.primitiveLeaves.getBlock()) {
			this.primitiveLeaves = primitiveLeaves.getDefaultState();
			this.family.removeConnectableVanillaLeaves(this.primitiveLeavesConnectable);
			this.family.addConnectableVanillaLeaves(state -> state.getBlock() == primitiveLeaves);
		}
	}

	public ResourceLocation getPrimitiveLeavesRegName() {
		return primitiveLeavesRegName;
	}

	public void setPrimitiveLeavesRegName(ResourceLocation primitiveLeavesRegName) {
		this.primitiveLeavesRegName = primitiveLeavesRegName;
	}

	/**
	 * Gets {@link ItemStack} of the primitive (vanilla) leaves (for things like when it's sheared).
	 *
	 * @return The {@link ItemStack} object.
	 */
	public ItemStack getPrimitiveLeavesItemStack() {
		return new ItemStack(Item.BLOCK_TO_ITEM.get(getPrimitiveLeaves().getBlock()));
	}

	public Optional<DynamicLeavesBlock> getDynamicLeavesBlock() {
		return Optional.ofNullable(this.dynamicLeavesBlock);
	}

	protected DynamicLeavesBlock createDynamicLeaves (final AbstractBlock.Properties properties) {
		return new DynamicLeavesBlock(this, properties);
	}

	protected ResourceLocation getDynamicLeavesRegName() {
		return ResourceLocationUtils.suffix(this.getRegistryName(), "_leaves");
	}

	public void generateDynamicLeaves (final AbstractBlock.Properties properties) {
		this.dynamicLeavesBlock = RegistryHandler.addBlock(this.getDynamicLeavesRegName(), this.createDynamicLeaves(properties));
		LeavesPaging.addLeavesBlockForModId(this.dynamicLeavesBlock, this.getRegistryName().getNamespace());
	}
	
	public LeavesProperties setDynamicLeavesState(BlockState state) {
		//Cache all the blockStates to speed up worldgen
		dynamicLeavesBlockHydroStates[0] = Blocks.AIR.getDefaultState();
		for(int i = 1; i <= maxHydro; i++) {
			dynamicLeavesBlockHydroStates[i] = state.with(DynamicLeavesBlock.DISTANCE, i);
		}
		
		return this;
	}
	
	public BlockState getDynamicLeavesState() {
		return dynamicLeavesBlockHydroStates[maxHydro];
	}
	
	public BlockState getDynamicLeavesState(int hydro) {
		return dynamicLeavesBlockHydroStates[MathHelper.clamp(hydro, 0, maxHydro)];
	}

	public boolean hasDynamicLeavesBlock() {
		if (getDynamicLeavesState() == null) return false;
		return getDynamicLeavesState().getBlock() instanceof DynamicLeavesBlock;
	}

	/**
	 * Used by the {@link DynamicLeavesBlock} to find out if it can pull hydro from a branch.
	 *
	 * @return The {@link Family} for these {@link LeavesProperties}.
	 */
	public Family getFamily() {
		return family;
	}

	/**
	 * Sets the type of tree these leaves connect to.
	 *
	 * @param family The {@link Family} object to set.
	 * @return This {@link LeavesProperties} object.
	 */
	public LeavesProperties setFamily(Family family) {
		this.family = family;
		if (family.isFireProof()) {
			flammability = 0;
			fireSpreadSpeed = 0;
		}
		return this;
	}

	public int getFlammability() {
		return flammability;
	}

	public void setFlammability(int flammability) {
		this.flammability = flammability;
	}

	public int getFireSpreadSpeed() {
		return fireSpreadSpeed;
	}

	public void setFireSpreadSpeed(int fireSpreadSpeed) {
		this.fireSpreadSpeed = fireSpreadSpeed;
	}

	/**
	 * Gets the smother leaves max - the maximum amount of leaves in a stack before the
	 * bottom-most leaf block dies. Can beset to zero to disable smothering. [default = 4]
	 *
	 * @return The smother leaves max.
	 */
	public int getSmotherLeavesMax() {
		return this.smotherLeavesMax;
	}

	public void setSmotherLeavesMax(int smotherLeavesMax) {
		this.smotherLeavesMax = smotherLeavesMax;
	}

	/**
	 * Gets the minimum amount of light necessary for a leaves block to be created. [default = 13]
	 *
	 * @return The minimum light requirement.
	 */
	public int getLightRequirement() {
		return this.lightRequirement;
	}

	public void setLightRequirement(int lightRequirement) {
		this.lightRequirement = lightRequirement;
	}

	/**
	 * Gets the {@link CellKit}, which is for leaves automata.
	 *
	 * @return The {@link CellKit} object.
	 */
	public CellKit getCellKit() {
		return cellKit;
	}

	public void setCellKit(CellKit cellKit) {
		this.cellKit = cellKit;
	}

	public boolean isConnectAnyRadius() {
		return connectAnyRadius;
	}

	public void setConnectAnyRadius(boolean connectAnyRadius) {
		this.connectAnyRadius = connectAnyRadius;
	}

	public AbstractBlock.Properties getDefaultBlockProperties() {
		return AbstractBlock.Properties.create(Material.LEAVES).hardnessAndResistance(0.2F)
				.tickRandomly().sound(SoundType.PLANT).notSolid().setAllowsSpawn((s, r, p, e) -> e == EntityType.OCELOT || e == EntityType.PARROT)
				.setSuffocates((s, r, p) -> false).setBlocksVision((s, r, p) -> false);
	}

	/**
	 * Allows the leaves to perform a specific needed behavior or to optionally cancel the update.
	 *
	 * @param worldIn The {@link World} object.
	 * @param pos The {@link BlockPos} of the leaves.
	 * @param state The {@link BlockState} of the leaves.
	 * @param rand A {@link Random} object.
	 * @return return true to allow the normal DynamicLeavesBlock update to occur
	 */
	public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) { return true; }

	public int getRadiusForConnection(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		return (fromRadius == 1 || this.connectAnyRadius) && from.getFamily().isCompatibleDynamicLeaves(blockAccess.getBlockState(pos), blockAccess, pos) ? 1 : 0;
	}

	///////////////////////////////////////////
	// Leaves colours
	///////////////////////////////////////////

	protected Integer colorNumber;
	protected String colorString;

	@OnlyIn(Dist.CLIENT)
	private IBlockColor colorMultiplier;

	@OnlyIn(Dist.CLIENT)
	public int foliageColorMultiplier(BlockState state, IBlockDisplayReader world, BlockPos pos) {
		return colorMultiplier.getColor(state, world, pos, -1);
	}

	@OnlyIn(Dist.CLIENT)
	private void processColor() {
		int color = -1;
		if (this.colorNumber != null) {
			color = this.colorNumber;
		} else if (this.colorString != null) {
			String code = this.colorString;
			if(code.startsWith("@")) {
				code = code.substring(1);
				if ("biome".equals(code)) { // Built in code since we need access to super.
					this.colorMultiplier = (state, world, pos, t) -> ((IWorld) world).getBiome(pos).getFoliageColor();
					return;
				}

				IBlockColor blockColor = BlockColorMultipliers.find(code);
				if(blockColor != null) {
					return;
				} else {
					LogManager.getLogger().error("Error: ColorMultiplier resource '{}' could not be found.", code);
				}
			} else {
				color = Color.decode(code).getRGB();
			}
		}
		int c = color;
		this.colorMultiplier = (s, w, p, t) -> c == -1 ? Minecraft.getInstance().getBlockColors().getColor(getPrimitiveLeaves(), w, p, 0) : c;
	}

	@OnlyIn(Dist.CLIENT)
	public static void postInitClient() {
		REGISTRY.getAll().forEach(LeavesProperties::processColor);
	}

	@Override
	public String toString() {
		return "LeavesProperties{" +
				"primitiveLeaves=" + primitiveLeaves +
				", tree=" + family + '}';
	}

	public String getDisplayString() {
		return "LeavesProperties{" +
				"registryName=" + this.getRegistryName() +
				", primitiveLeaves=" + primitiveLeaves +
				", primitiveLeavesRegistryName=" + primitiveLeavesRegName +
				", cellKit=" + cellKit +
				", tree=" + family +
				", dynamicLeavesBlockHydroStates=" + Arrays.toString(dynamicLeavesBlockHydroStates) +
				", flammability=" + flammability +
				", fireSpreadSpeed=" + fireSpreadSpeed +
				'}';
	}
}
