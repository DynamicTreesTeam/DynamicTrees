package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.data.Generator;
import com.ferreusveritas.dynamictrees.api.data.LeavesStateGenerator;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.cells.CellKits;
import com.ferreusveritas.dynamictrees.client.BlockColorMultipliers;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Resettable;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.ColorUtil;
import com.ferreusveritas.dynamictrees.util.MutableLazyValue;
import com.ferreusveritas.dynamictrees.util.Optionals;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class provides a means of holding individual properties for leaves.  This is necessary since leaves can contain
 * sub blocks that may behave differently.  Each leaves properties object must have a reference to a tree family.
 *
 * @author ferreusveritas
 */
public class LeavesProperties extends RegistryEntry<LeavesProperties> implements Resettable<LeavesProperties> {

    public static final Codec<LeavesProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString()).forGetter(LeavesProperties::getRegistryName))
            .apply(instance, LeavesProperties::new));

    public static final LeavesProperties NULL_PROPERTIES = new LeavesProperties() {
        @Override
        public LeavesProperties setFamily(Family family) {
            return this;
        }

        @Override
        public Family getFamily() {
            return Family.NULL_FAMILY;
        }

        @Override
        public BlockState getPrimitiveLeaves() {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public ItemStack getPrimitiveLeavesItemStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public LeavesProperties setDynamicLeavesState(BlockState state) {
            return this;
        }

        @Override
        public BlockState getDynamicLeavesState() {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public BlockState getDynamicLeavesState(int hydro) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public CellKit getCellKit() {
            return CellKit.NULL_CELL_KIT;
        }

        @Override
        public int getFlammability() {
            return 0;
        }

        @Override
        public int getFireSpreadSpeed() {
            return 0;
        }

        @Override
        public int getSmotherLeavesMax() {
            return 0;
        }

        @Override
        public int getLightRequirement() {
            return 15;
        }

        @Override
        public boolean updateTick(Level worldIn, BlockPos pos, BlockState state, RandomSource rand) {
            return false;
        }
    }.setRegistryName(DTTrees.NULL).setBlockRegistryName(DTTrees.NULL);

    /**
     * Central registry for all {@link LeavesProperties} objects.
     */
    public static final TypedRegistry<LeavesProperties> REGISTRY = new TypedRegistry<>(LeavesProperties.class, NULL_PROPERTIES, new TypedRegistry.EntryType<>(CODEC));

    protected static final int maxHydro = 7;

    /**
     * The primitive (vanilla) leaves are used for many purposes including rendering, drops, and some other basic
     * behavior.
     */
    protected BlockState primitiveLeaves;

    /**
     * The {@link CellKit}, which is for leaves automata.
     */
    protected CellKit cellKit;

    protected Family family;
    protected BlockState[] dynamicLeavesBlockHydroStates = new BlockState[maxHydro + 1];
    protected int flammability = 60;// Mimic vanilla leaves
    protected int fireSpreadSpeed = 30;// Mimic vanilla leaves

    protected int smotherLeavesMax = 4;
    protected int lightRequirement = 13;
    protected AgeingConfiguration ageingConfiguration = AgeingConfiguration.ALWAYS;
    protected boolean connectAnyRadius = false;

    protected boolean requiresShears = true;

    private LeavesProperties() {
    }

    public LeavesProperties(final ResourceLocation registryName) {
        this(null, registryName);
    }

    public LeavesProperties(@Nullable final BlockState primitiveLeaves, final ResourceLocation registryName) {
        this(primitiveLeaves, CellKits.DECIDUOUS, registryName);
    }

    public LeavesProperties(@Nullable final BlockState primitiveLeaves, final CellKit cellKit, final ResourceLocation registryName) {
        this.family = Family.NULL_FAMILY;
        this.primitiveLeaves = primitiveLeaves != null ? primitiveLeaves : BlockStates.AIR;
        this.cellKit = cellKit;
        this.setRegistryName(registryName);
        this.blockRegistryName = ResourceLocationUtils.suffix(registryName, this.getBlockRegistryNameSuffix());
    }

    ///////////////////////////////////////////
    // PRIMITIVE LEAVES BLOCK
    ///////////////////////////////////////////

    /**
     * Gets the primitive (vanilla) leaves for these {@link LeavesProperties}.
     *
     * @return The {@link BlockState} for the primitive leaves.
     */
    public BlockState getPrimitiveLeaves() {
        return primitiveLeaves;
    }

    public Optional<Block> getPrimitiveLeavesBlock() {
        return Optionals.ofBlock(this.primitiveLeaves.getBlock());
    }

    public void setPrimitiveLeaves(final Block primitiveLeaves) {
        if (this.primitiveLeaves == null || primitiveLeaves != this.primitiveLeaves.getBlock()) {
            this.primitiveLeaves = primitiveLeaves.defaultBlockState();
        }
    }

    /**
     * Gets {@link ItemStack} of the primitive (vanilla) leaves (for things like when it's sheared).
     *
     * @return The {@link ItemStack} object.
     */
    public ItemStack getPrimitiveLeavesItemStack() {
        return new ItemStack(Item.BY_BLOCK.get(getPrimitiveLeaves().getBlock()));
    }

    ///////////////////////////////////////////
    // DYNAMIC LEAVES BLOCK
    ///////////////////////////////////////////

    /**
     * The registry name for the leaves block. This allows for built-in compatibility where the dynamic leaves may
     * otherwise share the same name as their regular leaves block.
     */
    private ResourceLocation blockRegistryName;

    /**
     * Gets the {@link #blockRegistryName} for this {@link LeavesProperties} object.
     *
     * @return The {@link #blockRegistryName} for this {@link LeavesProperties} object.
     */
    public ResourceLocation getBlockRegistryName() {
        return this.blockRegistryName;
    }

    /**
     * Sets the {@link #blockRegistryName} for this {@link LeavesProperties} object to the specified {@code
     * blockRegistryName}.
     *
     * @param blockRegistryName The new {@link ResourceLocation} object to set.
     * @return This {@link LeavesProperties} object for chaining.
     */
    public LeavesProperties setBlockRegistryName(ResourceLocation blockRegistryName) {
        this.blockRegistryName = blockRegistryName;
        return this;
    }

    /**
     * Returns a default suffix for {@link #blockRegistryName}. Note that this will be overridden if the {@link
     * #blockRegistryName} is changed in the Json.
     *
     * @return A default suffix for {@link #blockRegistryName}.
     */
    protected String getBlockRegistryNameSuffix() {
        return "_leaves";
    }

    public Optional<DynamicLeavesBlock> getDynamicLeavesBlock() {
        Block block = this.getDynamicLeavesState().getBlock();
        return Optional.ofNullable(block instanceof DynamicLeavesBlock ? (DynamicLeavesBlock) block : null);
    }

    protected DynamicLeavesBlock createDynamicLeaves(final BlockBehaviour.Properties properties) {
        return new DynamicLeavesBlock(this, properties);
    }

    public void generateDynamicLeaves(final BlockBehaviour.Properties properties) {
        RegistryHandler.addBlock(this.blockRegistryName, () -> this.createDynamicLeaves(properties));
    }

    public LeavesProperties setDynamicLeavesState(BlockState state) {
        //Cache all the blockStates to speed up worldgen
        dynamicLeavesBlockHydroStates[0] = Blocks.AIR.defaultBlockState();
        for (int i = 1; i <= maxHydro; i++) {
            dynamicLeavesBlockHydroStates[i] = state.setValue(DynamicLeavesBlock.DISTANCE, i);
        }
        return this;
    }

    public BlockState getDynamicLeavesState() {
        return getDynamicLeavesState(getCellKit().getDefaultHydration());
    }

    public BlockState getDynamicLeavesState(int hydro) {
        return Optional.ofNullable(dynamicLeavesBlockHydroStates[Mth.clamp(hydro, 0, maxHydro)])
                .orElse(Blocks.AIR.defaultBlockState());
    }

    ///////////////////////////////////////////
    // PROPERTIES
    ///////////////////////////////////////////

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
     * Gets the smother leaves max - the maximum amount of leaves in a stack before the bottom-most leaf block dies. Can
     * beset to zero to disable smothering. [default = 4]
     *
     * @return the smother leaves max.
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
     * @return the minimum light requirement.
     */
    public int getLightRequirement() {
        return this.lightRequirement;
    }

    public void setLightRequirement(int lightRequirement) {
        this.lightRequirement = lightRequirement;
    }

    public enum AgeingConfiguration {
        ALWAYS(true, true),
        WORLDGEN_ONLY(true, false),
        GROWTH_ONLY(false, true),
        NEVER(false, false);

        private final boolean ageDuringWorldGen, ageDuringGrowth;

        AgeingConfiguration(boolean ageDuringWorldGen, boolean ageDuringGrowth) {
            this.ageDuringWorldGen = ageDuringWorldGen;
            this.ageDuringGrowth = ageDuringGrowth;
        }

        public boolean shouldAge(boolean worldGen) {
            if (worldGen) {
                return ageDuringWorldGen;
            } else {
                return ageDuringGrowth;
            }
        }
    }

    /**
     * @deprecated use {@link #shouldAge(boolean, BlockState)}
     */
    public boolean getDoesAge(boolean worldGen, BlockState state) {
        return shouldAge(worldGen, state);
    }

    /**
     * If the leaves block should tick and age. May return {@code false} to allow for dead leaves.
     *
     * @param worldGen {@code true} if called during world gen
     * @return {@code true} if the leaves block should tick and age
     */
    public boolean shouldAge(boolean worldGen, BlockState state) {
        return this.ageingConfiguration.shouldAge(worldGen);
    }

    /**
     * @deprecated use {@link #setAgeingConfiguration(AgeingConfiguration)}
     */
    @Deprecated
    public void setDoesAge(String doesAge) {
        try {
            this.ageingConfiguration = AgeingConfiguration.valueOf(doesAge.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ageing configuration \"" + doesAge + "\".");
        }
    }

    public void setAgeingConfiguration(AgeingConfiguration doesAge) {
        this.ageingConfiguration = doesAge;
    }

    private boolean canGrowOnGround = false;

    public void setCanGrowOnGround(boolean canGrowOnGround) {
        this.canGrowOnGround = canGrowOnGround;
    }

    public boolean canGrowOnGround() {
        return canGrowOnGround;
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

    public Material getDefaultMaterial() {
        return Material.LEAVES;
    }

    public BlockBehaviour.Properties getDefaultBlockProperties(final Material material, final MaterialColor materialColor) {
        return BlockBehaviour.Properties.of(material, materialColor)
                .strength(0.2F)
                //.harvestTool(ToolTypes.SHEARS)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isValidSpawn((s, r, p, e) -> e == EntityType.OCELOT || e == EntityType.PARROT)
                .isSuffocating((s, r, p) -> false)
                .isViewBlocking((s, r, p) -> false);
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////


    public boolean updateTick(Level worldIn, BlockPos pos, BlockState state, RandomSource rand) {
        return getDoesAge(false, state);
    }

    public int getRadiusForConnection(BlockState state, BlockGetter blockAccess, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        final int twigRadius = from.getFamily().getPrimaryThickness();
        return (fromRadius == twigRadius || this.connectAnyRadius) && from.getFamily().isCompatibleDynamicLeaves(from.getFamily().getCommonSpecies(), blockAccess.getBlockState(pos), blockAccess, pos) ? twigRadius : 0;
    }

    public boolean doRequireShears() {
        return requiresShears;
    }

    public void setRequiresShears(boolean requiresShears) {
        this.requiresShears = requiresShears;
    }

    public List<TagKey<Block>> defaultLeavesTags() {
        return Collections.singletonList(DTBlockTags.LEAVES);
    }

    protected final MutableLazyValue<Generator<DTBlockStateProvider, LeavesProperties>> stateGenerator =
            MutableLazyValue.supplied(LeavesStateGenerator::new);

    @Override
    public void generateStateData(DTBlockStateProvider provider) {
        // Generate leaves block state and model.
        this.stateGenerator.get().generate(provider, this);
    }

    ///////////////////////////////////////////
    // LEAVES COLORS
    ///////////////////////////////////////////

    protected Integer colorNumber;
    protected String colorString;

    public void setColorNumber(Integer colorNumber) {
        this.colorNumber = colorNumber;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    @OnlyIn(Dist.CLIENT)
    private BlockColor colorMultiplier;

    @OnlyIn(Dist.CLIENT)
    public int treeFallColorMultiplier(BlockState state, BlockAndTintGetter world, BlockPos pos) {
        return this.foliageColorMultiplier(state, world, pos);
    }

    @OnlyIn(Dist.CLIENT)
    public int foliageColorMultiplier(BlockState state, BlockAndTintGetter world, BlockPos pos) {
        if (colorMultiplier == null) {
            return 0x00FF00FF; //purple if broken
        }
        return colorMultiplier.getColor(state, world, pos, -1);
    }

    @OnlyIn(Dist.CLIENT)
    private void processColor() {
        int color = -1;
        if (this.colorNumber != null) {
            color = this.colorNumber;
        } else if (this.colorString != null) {
            String code = this.colorString;
            if (code.startsWith("@")) {
                code = code.substring(1);
                if ("biome".equals(code)) { // Built in code since we need access to super.
                    this.colorMultiplier = (state, world, pos, t) -> ((LevelAccessor) world).getBiome(pos).value().getFoliageColor();
                    return;
                }

                BlockColor blockColor = BlockColorMultipliers.find(code);
                if (blockColor != null) {
                    colorMultiplier = blockColor;
                    return;
                } else {
                    LogManager.getLogger().error("ColorMultiplier resource '{}' could not be found.", code);
                }
            } else {
                color = ColorUtil.decodeARGB32(code);
            }
        }
        int c = color;
        this.colorMultiplier = (s, w, p, t) -> c == -1 ? Minecraft.getInstance().getBlockColors().getColor(getPrimitiveLeaves(), w, p, 0) : c;
    }

    @OnlyIn(Dist.CLIENT)
    public static void postInitClient() {
        REGISTRY.getAll().forEach(LeavesProperties::processColor);
    }

    ///////////////////////////////////////////
    // MISC
    ///////////////////////////////////////////

    @Override
    public String toReloadDataString() {
        return this.getString(Pair.of("primitiveLeaves", this.primitiveLeaves), Pair.of("cellKit", this.cellKit),
                Pair.of("smotherLeavesMax", this.smotherLeavesMax), Pair.of("lightRequirement", this.lightRequirement),
                Pair.of("fireSpreadSpeed", this.fireSpreadSpeed), Pair.of("flammability", this.flammability),
                Pair.of("connectAnyRadius", this.connectAnyRadius));
    }

}
