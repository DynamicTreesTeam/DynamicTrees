package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.trees.IResettable;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.prefix;

/**
 * @author Max Hyper
 */
public class SoilProperties extends RegistryEntry<SoilProperties> implements IResettable<SoilProperties> {

    public static final Codec<SoilProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(DTResourceRegistries.RESOURCE_LOCATION.toString()).forGetter(SoilProperties::getRegistryName))
            .apply(instance, SoilProperties::new));

    public static final SoilProperties NULL_SOIL_PROPERTIES = new SoilProperties() {
        @Override
        public Block getPrimitiveSoilBlock() {
            return Blocks.AIR;
        }

        @Nullable
        @Override
        public RootyBlock getDynamicSoilBlock() {
            return null;
        }

        @Override
        public Integer getSoilFlags() {
            return 0;
        }

        @Override
        public void generateDynamicSoil(AbstractBlock.Properties properties) {
        }
    }.setRegistryName(DTTrees.NULL).setBlockRegistryName(DTTrees.NULL);

    /**
     * Central registry for all {@link LeavesProperties} objects.
     */
    public static final TypedRegistry<SoilProperties> REGISTRY = new TypedRegistry<>(SoilProperties.class, NULL_SOIL_PROPERTIES, new TypedRegistry.EntryType<>(CODEC));

    protected Block primitiveSoilBlock;
    protected RootyBlock dynamicSoilBlock;
    protected Integer soilFlags = 0;
    private ResourceLocation blockRegistryName;

    //used for null soil properties
    protected SoilProperties() {
    }

    //used for Dirt Helper registrations only
    protected SoilProperties(final Block primitiveBlock, ResourceLocation name, Integer soilFlags, boolean generate) {
        this(primitiveBlock, name);
        this.soilFlags = soilFlags;
        if (generate) {
            generateDynamicSoil(AbstractBlock.Properties.copy(primitiveBlock));
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

    protected void setPrimitiveSoilBlock(final Block primitiveSoil) {
        if (this.primitiveSoilBlock == null || primitiveSoil != this.primitiveSoilBlock.getBlock()) {
            this.primitiveSoilBlock = primitiveSoil;
        }
        SoilHelper.addSoilPropertiesToMap(this);
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

    @Nullable
    public RootyBlock getDynamicSoilBlock() {
        return dynamicSoilBlock;
    }

    public Optional<RootyBlock> getSoilBlock() {
        return Optional.ofNullable(dynamicSoilBlock);
    }

    public void generateDynamicSoil(AbstractBlock.Properties blockProperties) {
        setBlockRegistryNameIfNull();
        this.dynamicSoilBlock = RegistryHandler.addBlock(this.blockRegistryName, this.createDynamicSoil(blockProperties));
    }

    protected RootyBlock createDynamicSoil(AbstractBlock.Properties blockProperties) {
        return new RootyBlock(this, blockProperties);
    }

    public void setDynamicSoilBlock(RootyBlock rootyBlock) {
        this.dynamicSoilBlock = rootyBlock;
    }

    ///////////////////////////////////////////
    // MATERIAL
    ///////////////////////////////////////////

    public Material getDefaultMaterial() {
        return Material.DIRT;
    }

    public AbstractBlock.Properties getDefaultBlockProperties(final Material material, final MaterialColor materialColor) {
        return AbstractBlock.Properties.of(material, materialColor).strength(0.5F).sound(SoundType.GRAVEL);
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

    public void registerStatesAndModels(DTBlockStateProvider provider) {
        if (this.dynamicSoilBlock == Blocks.AIR || this.primitiveSoilBlock == Blocks.AIR) {
            return;
        }

        provider.getMultipartBuilder(this.dynamicSoilBlock)
                .part().modelFile(provider.models().getExistingFile(
                        provider.block(Objects.requireNonNull(this.primitiveSoilBlock.getRegistryName()))
                )).addModel().end()
                .part().modelFile(provider.models().getExistingFile(this.getRootsOverlayLocation())).addModel().end();
    }

    public ResourceLocation getRootsOverlayLocation() {
        return DynamicTrees.resLoc("block/roots");
    }

    //////////////////////////////
    // JAVA OBJECT STUFF
    //////////////////////////////

    @Override
    public String toString() {
        return getRegistryName().toString();
    }
}
