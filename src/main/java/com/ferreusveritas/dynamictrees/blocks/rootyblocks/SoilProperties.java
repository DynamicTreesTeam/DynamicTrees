package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.trees.IResettable;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public class SoilProperties extends RegistryEntry<SoilProperties> implements IResettable<SoilProperties> {

    public static final Codec<SoilProperties> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf(DTResourceRegistries.RESOURCE_LOCATION.toString()).forGetter(SoilProperties::getRegistryName))
            .apply(instance, SoilProperties::new));

    public static final SoilProperties NULL_PROPERTIES = new SoilProperties() {
        @Override public Block getPrimitiveSoilBlock() { return Blocks.AIR; }
        @Nullable @Override public RootyBlock getDynamicSoilBlock() { return null; }
        @Override public Integer getSoilFlags() { return 0; }
        @Override public void generateDynamicSoil() { }
    }.setRegistryName(DTTrees.NULL).setBlockRegistryName(DTTrees.NULL);

    /**
     * Central registry for all {@link LeavesProperties} objects.
     */
    public static final TypedRegistry<SoilProperties> REGISTRY = new TypedRegistry<>(SoilProperties.class, NULL_PROPERTIES, new TypedRegistry.EntryType<>(CODEC));

    protected Block primitiveSoilBlock;
    protected RootyBlock dynamicSoilBlock;

    protected Integer soilFlags;

    private ResourceLocation blockRegistryName;

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
        this.primitiveSoilBlock = primitiveBlock != null ? primitiveBlock : Blocks.AIR;
        this.setRegistryName(registryName);
        this.blockRegistryName = ResourceLocationUtils.prefix(registryName, this.getBlockRegistryNamePrefix());
    }

    public Block getPrimitiveSoilBlock(){
        return primitiveSoilBlock;
    }

    public ResourceLocation getBlockRegistryName() {
        return this.blockRegistryName;
    }
    public SoilProperties setBlockRegistryName(ResourceLocation blockRegistryName) {
        this.blockRegistryName = blockRegistryName;
        return this;
    }

    @Nullable
    public RootyBlock getDynamicSoilBlock (){
        return dynamicSoilBlock;
    }

    protected String getBlockRegistryNamePrefix() {
        return "rooty_";
    }

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

    public void setPrimitiveSoilBlock(final Block primitiveSoil) {
        if (this.primitiveSoilBlock == null || primitiveSoil != this.primitiveSoilBlock.getBlock())
            this.primitiveSoilBlock = primitiveSoil;
    }

    public void generateDynamicSoil () {
        this.dynamicSoilBlock = RegistryHandler.addBlock(this.blockRegistryName, this.createDynamicSoil());
    }
    protected RootyBlock createDynamicSoil () {
        return new RootyBlock(this);
    }

    public void setDynamicSoilBlock (RootyBlock rootyBlock){
        this.dynamicSoilBlock = rootyBlock;
    }

    @Override
    public String toString() {
        return getRegistryName().toString();
    }
}
