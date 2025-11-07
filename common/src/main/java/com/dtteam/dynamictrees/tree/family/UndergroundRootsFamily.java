package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.lazyvalue.MutableLazyValue;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.tree.species.UndergroundRootsSpecies;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.utility.ResourceLocationUtils.suffix;

public class UndergroundRootsFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(UndergroundRootsFamily::new);
    private AerialRootsSoilProperties defaultSoil;
    private Supplier<BranchBlock> roots;
    private Supplier<Item> rootsItem;
    private Block primitiveRoots, primitiveRootsFilled, primitiveRootsCovered;

    public UndergroundRootsFamily(ResourceLocation name) {
        super(name);
    }

    @Override
    public void setCommonSpecies(Species species) {
        super.setCommonSpecies(species);
        if (!(species instanceof UndergroundRootsSpecies)) {
            LogManager.getLogger().warn("Common species {} for Underground Roots Family {} is not of type {}", species.getRegistryName(), getRegistryName(), UndergroundRootsSpecies.class);
        }
    }

    ///////////////////////////////////////////
    // DEFAULT SOIL
    ///////////////////////////////////////////

    public void setDefaultSoil(SoilProperties defaultSoil) {
        if (!(defaultSoil instanceof AerialRootsSoilProperties))
            throw new RuntimeException("Soil "+ defaultSoil.toString() +" for Underground Roots Family "+ this +" is not of type "+ AerialRootsSoilProperties.class);
        this.defaultSoil = (AerialRootsSoilProperties) defaultSoil;
        this.defaultSoil.setFamily(this);
    }

    public AerialRootsSoilProperties getDefaultSoil() {
        return defaultSoil;
    }

    ///////////////////////////////////////////
    // DYNAMIC ROOTS
    ///////////////////////////////////////////

    @Override
    public void setupBlocks() {
        super.setupBlocks();
        this.setRoots(this.createRoots(this.getBranchName()));
        this.setRootsItem(this.createRootsItem(this.getBranchName(), this.roots));
    }

    protected Supplier<BranchBlock> createRoots(final ResourceLocation name) {
        return RegistryHandler.addBlock(suffix(name, getRootsNameSuffix()), () -> createRootsBlock(name));
    }
    protected BranchBlock createRootsBlock(ResourceLocation name) {
        final BasicRootsBlock branch = new BasicRootsBlock(name, this.getProperties());
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }
    public Supplier<BlockItem> createRootsItem(final ResourceLocation registryName, final Supplier<BranchBlock> rootsSup) {
        return RegistryHandler.addItem(suffix(registryName, getRootsNameSuffix()), () -> new BlockItem(rootsSup.get(), new Item.Properties()));
    }

    protected String getRootsNameSuffix() {
        return BasicRootsBlock.NAME_SUFFIX;
    }

    public Family setRoots(final Supplier<BranchBlock> branchSup) {
        this.roots = setupBranch(branchSup, false);
        return this;
    }
    @SuppressWarnings("unchecked")
    protected <T extends Item> Family setRootsItem(Supplier<T> branchItemSup) {
        this.rootsItem = (Supplier<Item>) branchItemSup;
        return this;
    }
    public Optional<BranchBlock> getRoots() {
        return Optionals.ofBlock(roots);
    }
    public Optional<Item> getRootsItem() {
        return Optionals.ofItem(rootsItem);
    }

    @Override
    public Optional<BranchBlock> getBranchForRootsPlacement(LevelAccessor level, Species species, BlockPos pos) {
        return getRoots();
    }

    ///////////////////////////////////////////
    // DATA GENERATION
    ///////////////////////////////////////////

    protected final MutableLazyValue<Generator<DTDataProvider.BlockState, Family>> rootsStateGenerator =
            MutableLazyValue.supplied(blockStateGenerators.get(
                    DynamicTrees.location("roots")
            ));
    protected final MutableLazyValue<Generator<DTDataProvider.ItemModel, Family>> rootsItemModelGenerator =
            MutableLazyValue.supplied(itemModelGenerators.get(
                    DynamicTrees.location("roots_item")
            ));

    @Override
    public void generateStateData(DTDataProvider.BlockState provider) {
        super.generateStateData(provider);
        this.rootsStateGenerator.get().generate(provider, this);
    }

    @Override
    public void generateItemModelData(DTDataProvider.ItemModel provider) {
        super.generateItemModelData(provider);
        this.rootsItemModelGenerator.get().generate(provider, this);
    }

    public ResourceLocation getBranchItemParentLocation() {
        return DynamicTrees.location("item/branch");
    }

    public List<TagKey<Block>> defaultRootsTags() {
        return Collections.singletonList(DTBlockTags.ROOTS);
    }

    /**
     * {@code null} = can harvest with hand
     */
    @Nullable
    public Tier getDefaultRootsHarvestTier() {
        return null;
    }

    protected int rootSystemSoilTypeFlags = 0;

    @Override
    public boolean isAcceptableSoilForRootSystem(BlockState soilBlockState){
        return soilBlockState.getBlock() instanceof AerialRootsSoilProperties.RootSoilBlock || SoilHelper.isSoilAcceptable(soilBlockState, rootSystemSoilTypeFlags);
    }

    public Family addAcceptableSoilsForRootSystem(String... soilTypes) {
        rootSystemSoilTypeFlags |= SoilHelper.getSoilFlags(soilTypes);
        return this;
    }

    @Override
    public boolean hasRootSystem() {
        return true;
    }

    ///////////////////////////////////////////
    // PRIMITIVE ROOTS
    ///////////////////////////////////////////

    private int primaryRootThickness = 2;
    private int secondaryRootThickness = 3;
    private int supportedRootThicknessExtra = 2;

    /**
     * Thickness of tips of the root system.
     * By default, most trees do not have one, so we return the regular primary thickness.
     */
    public int getPrimaryRootThickness() {
        return primaryRootThickness;
    }

    /**
     * Thickness of the root connected to tips in the root system.
     * By default, most trees do not have one, so we return the regular secondary thickness.
     */
    public int getSecondaryRootThickness() {
        return secondaryRootThickness;
    }

    public int getSupportedRootThicknessExtra() {
        return supportedRootThicknessExtra;
    }
    public void setPrimaryRootThickness(int primaryRootThickness) {
        this.primaryRootThickness = primaryRootThickness;
    }
    public void setSecondaryRootThickness(int secondaryRootThickness) {
        this.secondaryRootThickness = secondaryRootThickness;
    }
    public void setSupportedRootThicknessExtra(int supportedRootThicknessExtra) {
        this.supportedRootThicknessExtra = supportedRootThicknessExtra;
    }

    public void setPrimitiveRoots(Block primitiveRoots) {
        this.primitiveRoots = primitiveRoots;
        if (this.roots != null) {
            this.roots.get().setPrimitiveLogDrops(new ItemStack(primitiveRoots));
        }
    }
    public void setPrimitiveRootsFilled(Block primitiveRootsFilled) {
        this.primitiveRootsFilled = primitiveRootsFilled;
    }
    public void setPrimitiveRootsCovered(Block primitiveRootsCovered) {
        this.primitiveRootsCovered = primitiveRootsCovered;
    }

    public Optional<Block> getPrimitiveRoots() {
        return Optionals.ofBlock(primitiveRoots);
    }
    public Optional<Block> getPrimitiveFilledRoots() {
        return Optionals.ofBlock(primitiveRootsFilled);
    }
    public Optional<Block> getPrimitiveCoveredRoots() {
        return Optionals.ofBlock(primitiveRootsCovered);
    }

    @Override
    public void addGeneratedBlockTags (Function<TagKey<Block>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block>> tagAppender){
        super.addGeneratedBlockTags(tagAppender);
        //Create roots tag and root harvest tag if the family is mangrove-like.
        getRoots().ifPresent(roots -> {
            this.tierTag(getDefaultRootsHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(roots));
            defaultRootsTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(roots);
                } else {
                    tagAppender.apply(tag).addOptional(BuiltInRegistries.BLOCK.getKey(roots));
                }
            });
        });
    }
}
