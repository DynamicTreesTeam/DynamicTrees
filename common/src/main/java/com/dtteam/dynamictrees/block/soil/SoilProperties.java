package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.lazyvalue.MutableLazyValue;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.treepack.Resettable;
import com.dtteam.dynamictrees.utility.Optionals;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.utility.IdentifierUtils.prefix;

/**
 * @author Max Hyper
 */
public class SoilProperties extends RegistryEntry<SoilProperties> implements Resettable<SoilProperties> {

    public static final HashMap<Identifier, Supplier<Generator<DTDataProvider.BlockState, SoilProperties>>> blockStateGenerators = new HashMap<>();
    public static final HashMap<Identifier, Supplier<Generator<DTDataProvider.ItemModel, SoilProperties>>> itemModelGenerators = new HashMap<>();
    public static final HashMap<Identifier, Supplier<Generator<DTDataProvider.Language, SoilProperties>>> languageGenerators = new HashMap<>();

    public static final Codec<SoilProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Identifier.CODEC.fieldOf(TypedRegistry.RESOURCE_LOCATION.toString()).forGetter(SoilProperties::getRegistryName))
            .apply(instance, SoilProperties::new));

    public static final SoilProperties NULL_SOIL_PROPERTIES = new SoilProperties() {
        @Override
        public Block getPrimitiveSoilBlock() {
            return Blocks.AIR;
        }

        @Override
        public Optional<SoilBlock> getBlock() {
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
    protected Supplier<SoilBlock> block;
    protected Integer soilFlags = 0;
    private Identifier blockRegistryName;
    protected boolean generateBlock = true;
    protected List<String> onlyIfLoaded = new ArrayList<>();
    protected int foliageTintIndex = 0;
    protected int rootsTintIndex = 1;

    //used for null soil properties
    protected SoilProperties() {
    }

    //used for Dirt Helper registrations only
    protected SoilProperties(final Block primitiveBlock, Identifier name, Integer soilFlags, boolean generate) {
        this(primitiveBlock, name);
        this.soilFlags = soilFlags;
        if (generate) {
            generateBlock(BlockBehaviour.Properties.ofFullCopy(primitiveBlock));
        }
    }

    public SoilProperties(final Identifier registryName) {
        this(null, registryName);
    }

    public SoilProperties(@Nullable final Block primitiveBlock, final Identifier registryName) {
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
        if (this.primitiveSoilBlock != null && (primitiveSoil == null || primitiveSoil == Blocks.AIR))
            return;
        if (this.primitiveSoilBlock == null || primitiveSoil != this.primitiveSoilBlock) {
            this.primitiveSoilBlock = primitiveSoil;
        }
        SoilHelper.addSoilPropertiesToMap(this);
    }

    /**
     * Allows to veto a soil block based on the BlockState.
     */
    @SuppressWarnings("unused")
    public boolean isValidState(BlockState primitiveSoilState){
        return true;
    }

    /**
     * primitiveSoilState should always be this soil's primitive block, but if used on, verify anyways.
     * @return the BlockState of the rooty soil.
     */
    public BlockState getSoilState(BlockState primitiveSoilState, int fertility, boolean requireTileEntity){
        return block.get().defaultBlockState().setValue(SoilBlock.FERTILITY, fertility).setValue(SoilBlock.IS_VARIANT, requireTileEntity);
    }

    /**
     * @return the BlockState of the primitive soil that is set when it is no longer supporting a tree.
     */
    @SuppressWarnings("unused")
    public BlockState getPrimitiveSoilState (BlockState currentSoilState){
        return primitiveSoilBlock.defaultBlockState();
    }

    ///////////////////////////////////////////
    // ROOTY BLOCK
    ///////////////////////////////////////////

    protected String getBlockRegistryNamePrefix() {
        return "rooty_";
    }

    public Identifier getBlockRegistryName() {
        return this.blockRegistryName;
    }

    public SoilProperties setBlockRegistryName(Identifier blockRegistryName) {
        this.blockRegistryName = blockRegistryName;
        return this;
    }

    private void setBlockRegistryNameIfNull() {
        if (this.blockRegistryName == null) {
            this.blockRegistryName = prefix(this.getRegistryName(), this.getBlockRegistryNamePrefix());
        }
    }

    public Optional<SoilBlock> getBlock() {
        if (block == null) return Optional.empty();
        return Optionals.ofBlock(block.get());
    }

    public void generateBlock(BlockBehaviour.Properties blockProperties) {
        setBlockRegistryNameIfNull();
        this.block = RegistryHandler.addBlock(this.blockRegistryName, () -> this.createBlock(blockProperties));
    }

    protected SoilBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new SoilBlock(this, blockProperties);
    }

    public void setBlock(SoilBlock soilBlock) {
        this.block = () -> soilBlock;
    }

    public boolean shouldGenerateBlock() {
        return generateBlock;
    }

    public void setGenerateBlock(boolean hasSubstitute) {
        this.generateBlock = hasSubstitute;
    }

    public void setSubstitute(SoilProperties substituteSoilProperties) {
        this.block = () -> substituteSoilProperties.block.get();
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

    @Deprecated(forRemoval = true)
    public MapColor getDefaultMapColor() {
        return MapColor.DIRT;
    }

    public BlockBehaviour.Properties getDefaultBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .sound(SoundType.GRAVEL)
                .strength(0.5F);
    }

    @Deprecated(forRemoval = true)
    public BlockBehaviour.Properties getDefaultBlockProperties(final MapColor mapColor) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL);
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
    // DATA GENERATION
    ///////////////////////////////////////////

    protected final MutableLazyValue<Generator<DTDataProvider.BlockState, SoilProperties>> soilStateGenerator =
            MutableLazyValue.supplied(blockStateGenerators.get(
                    DynamicTrees.location("soil")
            ));

    @Override
    public void generateStateData(DTDataProvider.BlockState provider) {
        // Generate soil state and model.
        this.soilStateGenerator.get().generate(provider, this);
    }

    protected HashMap<String, Identifier> textureOverrides = new HashMap<>();
    protected HashMap<String, Identifier> modelOverrides = new HashMap<>();
    public static final String ROOTS = "roots";
    public static final String SOIL_BLOCK = "soil_block";

    public void setTextureOverrides(Map<String, Identifier> textureOverrides) {
        this.textureOverrides.putAll(textureOverrides);
    }
    public Optional<Identifier> getTexturePath(String key) {
        return Optional.ofNullable(textureOverrides.getOrDefault(key, null));
    }
    public void setModelOverrides(Map<String, Identifier> modelOverrides) {
        this.modelOverrides.putAll(modelOverrides);
    }
    public Optional<Identifier> getModelPath(String key) {
        return Optional.ofNullable(modelOverrides.getOrDefault(key, null));
    }

    public Identifier getRootsOverlayModelLocation() {
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
