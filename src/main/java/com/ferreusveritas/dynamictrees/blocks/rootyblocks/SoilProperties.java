package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.data.Generator;
import com.ferreusveritas.dynamictrees.api.data.SoilStateGenerator;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.trees.Resettable;
import com.ferreusveritas.dynamictrees.util.MutableLazyValue;
import com.ferreusveritas.dynamictrees.util.Optionals;
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
import java.util.Optional;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.prefix;

/**
 * @author Max Hyper
 */
public class SoilProperties extends RegistryEntry<SoilProperties> implements Resettable<SoilProperties> {

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
    protected boolean substitute;

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

    public Optional<Block> getPrimitiveSoilBlockOptional() {
        return Optionals.ofBlock(primitiveSoilBlock);
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
        return Optional.ofNullable(this.dynamicSoilBlock == Blocks.AIR ? null : this.dynamicSoilBlock);
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

    public boolean isSubstitute() {
        return substitute;
    }

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
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

    ///////////////////////////////////////////
    // DATA GEN
    ///////////////////////////////////////////

    protected final MutableLazyValue<Generator<DTBlockStateProvider, SoilProperties>> soilStateGenerator =
            MutableLazyValue.supplied(SoilStateGenerator::new);

    @Override
    public void generateStateData(DTBlockStateProvider provider) {
        // Generate soil state and model.
        this.soilStateGenerator.get().generate(provider, this);
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
