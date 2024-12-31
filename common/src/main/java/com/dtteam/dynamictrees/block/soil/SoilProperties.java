package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.treepack.Resettable;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.util.Optionals;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.util.ResourceLocationUtils.prefix;

/**
 * @author Max Hyper
 */
public class SoilProperties extends RegistryEntry<SoilProperties> implements Resettable<SoilProperties> {

    public static final Codec<SoilProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString()).forGetter(SoilProperties::getRegistryName))
            .apply(instance, SoilProperties::new));

    public static final SoilProperties NULL_SOIL_PROPERTIES = new SoilProperties() {
        @Override
        public Block getPrimitiveSoilBlock() {
            return Blocks.AIR;
        }

        @Override
        public Optional<RootyBlock> getBlock() {
            return Optional.empty();
        }

        @Override
        public Integer getSoilFlags() {
            return 0;
        }

        @Override
        public void generateBlock(BlockBehaviour.Properties properties) {
        }
    }.setRegistryName(DynamicTrees.NULL).setBlockRegistryName(DynamicTrees.NULL);

    /**
     * Central registry for all {@link LeavesProperties} objects.
     */
    public static final TypedRegistry<SoilProperties> REGISTRY = new TypedRegistry<>(SoilProperties.class, NULL_SOIL_PROPERTIES, new TypedRegistry.EntryType<>(CODEC));

    protected Block primitiveSoilBlock;
    protected Supplier<RootyBlock> block;
    protected Integer soilFlags = 0;
    private ResourceLocation blockRegistryName;
    protected boolean hasSubstitute;
    protected List<String> onlyIfLoaded = new ArrayList<>();
    protected int foliageTintIndex = 0;
    protected int rootsTintIndex = 1;

    //used for null soil properties
    protected SoilProperties() {
    }

    //used for Dirt Helper registrations only
    protected SoilProperties(final Block primitiveBlock, ResourceLocation name, Integer soilFlags, boolean generate) {
        this(primitiveBlock, name);
        this.soilFlags = soilFlags;
        if (generate) {
            generateBlock(BlockBehaviour.Properties.ofFullCopy(primitiveBlock));
        }
    }

    public SoilProperties(final ResourceLocation registryName) {
        this(null, registryName);
    }

    public SoilProperties(@Nullable final Block primitiveBlock, final ResourceLocation registryName) {
        super(registryName);
        this.primitiveSoilBlock = primitiveBlock != null ? primitiveBlock : Blocks.AIR;
    }

    ///////////////////////////////////////////
    // PRIMITIVE SOIL
    ///////////////////////////////////////////

    public Block getPrimitiveSoilBlock() {
        return primitiveSoilBlock;
    }

    public Optional<Block> getPrimitiveSoilBlockOptional() {
        return Optionals.ofBlock(primitiveSoilBlock);
    }

    public void setPrimitiveSoilBlock(final Block primitiveSoil) {
        if (this.primitiveSoilBlock == null || primitiveSoil != this.primitiveSoilBlock) {
            this.primitiveSoilBlock = primitiveSoil;
        }
        SoilHelper.addSoilPropertiesToMap(this);
    }

    /**
     * Allows to veto a soil block based on the BlockState.
     */
    public boolean isValidState(BlockState primitiveSoilState){
        return true;
    }

    /**
     * primitiveSoilState should always be this soil's primitive block, but if used on, verify anyways.
     * @return the BlockState of the rooty soil.
     */
    public BlockState getSoilState(BlockState primitiveSoilState, int fertility, boolean requireTileEntity){
        return block.get().defaultBlockState().setValue(RootyBlock.FERTILITY, fertility).setValue(RootyBlock.IS_VARIANT, requireTileEntity);
    }

    /**
     * @return the BlockState of the primitive soil that is set when it is no longer supporting a tree.
     */
    public BlockState getPrimitiveSoilState (BlockState currentSoilState){
        return primitiveSoilBlock.defaultBlockState();
    }

    ///////////////////////////////////////////
    // ROOTY BLOCK
    ///////////////////////////////////////////

    protected String getBlockRegistryNamePrefix() {
        return "rooty_";
    }

    public ResourceLocation getBlockRegistryName() {
        return this.blockRegistryName;
    }

    public SoilProperties setBlockRegistryName(ResourceLocation blockRegistryName) {
        this.blockRegistryName = blockRegistryName;
        return this;
    }

    private void setBlockRegistryNameIfNull() {
        if (this.blockRegistryName == null) {
            this.blockRegistryName = prefix(this.getRegistryName(), this.getBlockRegistryNamePrefix());
        }
    }

    public Optional<RootyBlock> getBlock() {
        return Optionals.ofBlock(block.get());
    }

    public void generateBlock(BlockBehaviour.Properties blockProperties) {
        setBlockRegistryNameIfNull();
        this.block = RegistryHandler.addBlock(this.blockRegistryName, () -> this.createBlock(blockProperties));
    }

    protected RootyBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new RootyBlock(this, blockProperties);
    }

    public void setBlock(RootyBlock rootyBlock) {
        this.block = () -> rootyBlock;
    }

    public boolean hasSubstitute() {
        return hasSubstitute;
    }

    public void setHasSubstitute(boolean hasSubstitute) {
        this.hasSubstitute = hasSubstitute;
    }

    public void setFoliageTintIndex(int foliageTintIndex) {
        this.foliageTintIndex = foliageTintIndex;
    }

    public void setRootsTintIndex(int rootsTintIndex) {
        this.rootsTintIndex = rootsTintIndex;
    }

    ///////////////////////////////////////////
    // MATERIAL
    ///////////////////////////////////////////

    public MapColor getDefaultMapColor() {
        return MapColor.DIRT;
    }

    public BlockBehaviour.Properties getDefaultBlockProperties(final MapColor mapColor) {
        return BlockBehaviour.Properties.of().mapColor(mapColor).strength(0.5F).sound(SoundType.GRAVEL);
    }

    ///////////////////////////////////////////
    // SOIL FLAGS
    ///////////////////////////////////////////

    public Integer getSoilFlags() {
        return soilFlags;
    }

    public SoilProperties setSoilFlags(Integer adjFlag) {
        this.soilFlags = adjFlag;
        return this;
    }

    public SoilProperties addSoilFlags(Integer adjFlag) {
        this.soilFlags |= adjFlag;
        return this;
    }

    ///////////////////////////////////////////
    // DATA GEN
    ///////////////////////////////////////////

//    protected final MutableLazyValue<Generator<DTBlockStateProvider, SoilProperties>> soilStateGenerator =
//            MutableLazyValue.supplied(SoilStateGenerator::new);
//
//    @Override
//    public void generateStateData(DTBlockStateProvider provider) {
//        // Generate soil state and model.
//        this.soilStateGenerator.get().generate(provider, this);
//    }

    protected HashMap<String, ResourceLocation> textureOverrides = new HashMap<>();
    protected HashMap<String, ResourceLocation> modelOverrides = new HashMap<>();
    public static final String ROOTS = "roots";
    public static final String SOIL_BLOCK = "soil_block";

    public void setTextureOverrides(Map<String, ResourceLocation> textureOverrides) {
        this.textureOverrides.putAll(textureOverrides);
    }
    public Optional<ResourceLocation> getTexturePath(String key) {
        return Optional.ofNullable(textureOverrides.getOrDefault(key, null));
    }
    public void setModelOverrides(Map<String, ResourceLocation> modelOverrides) {
        this.modelOverrides.putAll(modelOverrides);
    }
    public Optional<ResourceLocation> getModelPath(String key) {
        return Optional.ofNullable(modelOverrides.getOrDefault(key, null));
    }

    public ResourceLocation getRootsOverlayModelLocation() {
        if (modelOverrides.containsKey(ROOTS)) return modelOverrides.get(ROOTS);
        return DynamicTrees.location("block/roots");
    }

    public List<TagKey<Block>> defaultSoilBlockTags() {
        return Collections.singletonList(DTBlockTags.ROOTY_SOIL);
    }

    public boolean isOnlyIfLoaded() {
        return !onlyIfLoaded.isEmpty();
    }
    public void setOnlyIfLoaded(String onlyIfLoaded) {
        this.onlyIfLoaded.add(onlyIfLoaded);
    }

//    public void addGeneratedBlockTags (Function<TagKey<Block>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block>> tagAppender){
//        // add rooty blocks to the rooty soil tag.
//        getBlock().ifPresent(rootyBlock ->
//                defaultSoilBlockTags().forEach(tag -> {
//                    if (!isOnlyIfLoaded()) {
//                        tagAppender.apply(tag).add(rootyBlock);
//                    } else {
//                        tagAppender.apply(tag).addOptional(BuiltInRegistries.BLOCK.getKey(rootyBlock));
//                    }
//                }));
//    }

    //////////////////////////////
    // JAVA OBJECT STUFF
    //////////////////////////////

    @Override
    public String toString() {
        return getRegistryName().toString();
    }

}
