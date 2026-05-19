package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.tree.BranchEntry;
import com.dtteam.dynamictrees.tree.species.AerialRootsSpecies;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.core.BlockPos;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.dtteam.dynamictrees.utility.IdentifierUtils.suffix;

public class AerialRootsFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(AerialRootsFamily::new);
    private AerialRootsSoilProperties defaultSoil;
    private Block primitiveRootsFilled, primitiveRootsCovered;
    public static final int ROOTS_INDEX = 2;

    public AerialRootsFamily(Identifier name) {
        super(name);
    }

    @Override
    public void setCommonSpecies(Species species) {
        super.setCommonSpecies(species);
        if (!(species instanceof AerialRootsSpecies)) {
            LogManager.getLogger().warn("Common species {} for Aerial Roots Family {} is not of type {}", species.getRegistryName(), getRegistryName(), AerialRootsSpecies.class);
        }
    }

    @Override
    public boolean hasRootSystem() {
        return true;
    }

    ///////////////////////////////////////////
    // DEFAULT SOIL
    ///////////////////////////////////////////

    public void setDefaultSoil(SoilProperties defaultSoil) {
        if (!(defaultSoil instanceof AerialRootsSoilProperties))
            throw new RuntimeException("Soil "+ defaultSoil.toString() +" for Aerial Roots Family "+ this +" is not of type "+ AerialRootsSoilProperties.class);
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
        branches.add(ROOTS_INDEX, new BranchEntry(this, getRootsName(""))
                .CreateBlock(this::createRoots)
                .CreateItem());
    }

    protected BranchBlock createRoots(Identifier name, BlockBehaviour.Properties properties) {
        return new BasicRootsBlock(name, properties);
    }

    protected Identifier getRootsName(final String prefix) {
        return this.getRegistryName().withPrefix(prefix).withSuffix(BasicRootsBlock.NAME_SUFFIX);
    }

    public Optional<BranchBlock> getRoots() {
        return getBranchBlock(ROOTS_INDEX);
    }
    public Optional<Item> getRootsItem() {
        return getBranchItem(ROOTS_INDEX);
    }

    @Override
    public Optional<BranchBlock> getBranchForRootsPlacement(LevelAccessor level, Species species, BlockPos pos) {
        return getRoots();
    }

    ///////////////////////////////////////////
    // DATA GENERATION
    ///////////////////////////////////////////

    @Override
    public List<Identifier> getBlockModelGenerators() {
        List<Identifier> list = new LinkedList<>(super.getBlockModelGenerators());
        list.add(DynamicTrees.location("roots"));
        return list;
    }

    @Override
    public List<Identifier> getItemModelGenerators() {
        List<Identifier> list = new LinkedList<>(super.getItemModelGenerators());
        list.add(DynamicTrees.location("roots_item"));
        return list;
    }

    public List<TagKey<Block>> defaultRootsTags() {
        return Collections.singletonList(DTBlockTags.ROOTS);
    }

    /**
     * {@code null} = can harvest with hand
     */
    @Nullable
    public ToolMaterial getDefaultRootsHarvestTier() {
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

    public void addRootTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation) {
        Identifier bark = suffix(primitiveLogLocation, "_side");
        Identifier rings = suffix(primitiveLogLocation, "_top");

        if (textureOverrides.containsKey(ROOTS_SIDE)) bark = textureOverrides.get(ROOTS_SIDE);
        if (textureOverrides.containsKey(ROOTS_TOP)) rings = textureOverrides.get(ROOTS_TOP);

        textureConsumer.accept("bark", bark);
        textureConsumer.accept("rings", rings);
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
        branches.get(ROOTS_INDEX).setPrimitiveBlock(primitiveRoots);
    }
    public void setPrimitiveRootsFilled(Block primitiveRootsFilled) {
        this.primitiveRootsFilled = primitiveRootsFilled;
    }
    public void setPrimitiveRootsCovered(Block primitiveRootsCovered) {
        this.primitiveRootsCovered = primitiveRootsCovered;
    }

    public Optional<Block> getPrimitiveRoots() {
        return getPrimitiveLog(ROOTS_INDEX);
    }
    public Optional<Block> getPrimitiveFilledRoots() {
        return Optionals.ofBlock(primitiveRootsFilled);
    }
    public Optional<Block> getPrimitiveCoveredRoots() {
        return Optionals.ofBlock(primitiveRootsCovered);
    }

    @Override
    public void addGeneratedBlockTags (Function<TagKey<Block>, TagAppender<Block, Block>> tagAppender){
        super.addGeneratedBlockTags(tagAppender);
        //Create roots tag and root harvest tag if the family is mangrove-like.
        getRoots().ifPresent(roots -> {
            this.tierTag(getDefaultRootsHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(roots));
            defaultRootsTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(roots);
                } else {
                    tagAppender.apply(tag).addOptional(roots);
                }
            });
        });
    }
}
