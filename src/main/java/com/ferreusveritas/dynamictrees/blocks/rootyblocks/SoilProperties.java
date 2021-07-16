package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.trees.IResettable;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.ferreusveritas.dynamictrees.util.json.ConfiguredGetter;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SoilProperties extends ConfigurableRegistryEntry<SoilProperties, ConfiguredSoilProperties<SoilProperties>> implements IResettable<SoilProperties> {

    public static final Codec<SoilProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(DTResourceRegistries.RESOURCE_LOCATION.toString()).forGetter(SoilProperties::getRegistryName))
            .apply(instance, SoilProperties::new));

    public static final SoilProperties NULL_SOIL_PROPERTIES = new SoilProperties() {
        @Override public Block getPrimitiveSoilBlock() { return Blocks.AIR; }
        @Nullable @Override public RootyBlock getDynamicSoilBlock() { return null; }
        @Override public Integer getSoilFlags() { return 0; }
        @Override public void generateDynamicSoil() { }
        @Override protected ConfiguredSoilProperties<SoilProperties> createDefaultConfiguration() { return null; }
        @Override public ConfiguredSoilProperties<SoilProperties> getDefaultConfiguration() {return ConfiguredSoilProperties.NULL_CONFIGURED_SOIL_PROPERTIES; }
     }.setRegistryName(DTTrees.NULL).setBlockRegistryName(DTTrees.NULL);

    /**
     * Central registry for all {@link LeavesProperties} objects.
     */
    public static final TypedRegistry<SoilProperties> REGISTRY = new TypedRegistry<>(SoilProperties.class, NULL_SOIL_PROPERTIES, new TypedRegistry.EntryType<>(CODEC));

    protected Block primitiveSoilBlock;
    protected RootyBlock dynamicSoilBlock;
    protected Integer soilFlags = 0;
    private ResourceLocation blockRegistryName;
    protected ConfiguredSoilProperties<? extends SoilProperties> configuration;

    //used for null soil properties
    protected SoilProperties() { }
    //used for Dirt Helper registrations only
    protected SoilProperties (final Block primitiveBlock, ResourceLocation name, Integer soilFlags, boolean generate){
        this(primitiveBlock, name);
        this.soilFlags = soilFlags;
        if (generate) generateDynamicSoil();
    }
    public SoilProperties (final ResourceLocation registryName){
        this(null, registryName);
    }
    public SoilProperties (@Nullable final Block primitiveBlock, final ResourceLocation registryName){
        super(registryName);
        this.primitiveSoilBlock = primitiveBlock != null ? primitiveBlock : Blocks.AIR;
    }

    ///////////////////////////////////////////
    // CONFIGURED PROPERTIES
    ///////////////////////////////////////////

    @Override
    protected ConfiguredSoilProperties<SoilProperties> createDefaultConfiguration() {
        return new ConfiguredSoilProperties<>(this);
    }

    @Override
    protected void registerProperties() { }

    public void setProperties(JsonObject obj){
        final ObjectFetchResult<ConfiguredSoilProperties<SoilProperties>> configured = new ObjectFetchResult<>();
        configured.setValue(getDefaultConfiguration());
        ConfiguredGetter.setProperties(configured, obj);
        getConfiguration().withAll(configured.getValue());
    }
    public void setProperties(ConfiguredSoilProperties<SoilProperties> properties){
        getConfiguration().withAll(properties);
    }

    public ConfiguredSoilProperties<SoilProperties> getConfiguration (){
        return SoilHelper.getConfiguredProperties(getPrimitiveSoilBlock());
    }

    ///////////////////////////////////////////
    // PRIMITIVE SOIL
    ///////////////////////////////////////////

    public Block getPrimitiveSoilBlock(){
        return primitiveSoilBlock;
    }

    public void setPrimitiveSoilBlock(final Block primitiveSoil) {
        if (this.primitiveSoilBlock == null || primitiveSoil != this.primitiveSoilBlock.getBlock())
            this.primitiveSoilBlock = primitiveSoil;
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

    private void setBlockRegistryNameIfNull(){
        if (this.blockRegistryName == null)
            this.blockRegistryName = ResourceLocationUtils.prefix(this.getRegistryName(), this.getBlockRegistryNamePrefix());
    }

    @Nullable
    public RootyBlock getDynamicSoilBlock (){
        return dynamicSoilBlock;
    }

    public void generateDynamicSoil () {
        setBlockRegistryNameIfNull();
        this.dynamicSoilBlock = RegistryHandler.addBlock(this.blockRegistryName, this.createDynamicSoil());
    }
    protected RootyBlock createDynamicSoil () {
        return new RootyBlock(getPrimitiveSoilBlock());
    }

    public void setDynamicSoilBlock (RootyBlock rootyBlock){
        this.dynamicSoilBlock = rootyBlock;
    }

    ///////////////////////////////////////////
    // SOIL FLAGS
    ///////////////////////////////////////////

    public Integer getSoilFlags(){
        return soilFlags;
    }

    public SoilProperties setSoilFlags(Integer adjFlag){
        this.soilFlags = adjFlag;
        return this;
    }

    public SoilProperties addSoilFlags(Integer adjFlag){
        this.soilFlags |= adjFlag;
        return this;
    }

    //////////////////////////////
    // JAVA OBJECT STUFF
    //////////////////////////////

    @Override
    public String toString() {
        return getRegistryName().toString();
    }
}
