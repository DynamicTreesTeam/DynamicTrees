package com.ferreusveritas.dynamictrees.tree.family;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.roots.BasicRootsBlock;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.util.Optionals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class MangroveFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(MangroveFamily::new);
    private AerialRootsSoilProperties defaultSoil;
    private BasicRootsBlock roots;
    private Block primitiveRoots, primitiveRootsFilled, primitiveRootsCovered;
    public MangroveFamily(ResourceLocation name) {
        super(name);
    }

    public void setRoots(BasicRootsBlock roots) {
        this.roots = roots;
    }
    public void setDefaultSoil(SoilProperties defaultSoil) {
        if (!(defaultSoil instanceof AerialRootsSoilProperties))
            throw new RuntimeException("Soil "+ defaultSoil.toString() +" for mangrove family "+ this +" is not of type "+ AerialRootsSoilProperties.class);
        this.defaultSoil = (AerialRootsSoilProperties) defaultSoil;
        this.defaultSoil.setFamily(this);
    }

    public AerialRootsSoilProperties getDefaultSoil() {
        return defaultSoil;
    }

    public BasicRootsBlock getRoots() {
        return roots;
    }

    public void setPrimitiveRoots(Block primitiveRoots) {
        this.primitiveRoots = primitiveRoots;
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

    public Optional<Block>  getPrimitiveRootsFilled() {
        return Optionals.ofBlock(primitiveRootsFilled);
    }

    public Optional<Block>  getPrimitiveRootsCovered() {
        return Optionals.ofBlock(primitiveRootsCovered);
    }
}
