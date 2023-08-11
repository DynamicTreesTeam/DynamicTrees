package com.ferreusveritas.dynamictrees.tree.family;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.roots.BasicRootsBlock;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import net.minecraft.resources.ResourceLocation;

public class MangroveFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(MangroveFamily::new);

    private AerialRootsSoilProperties defaultSoil;
    private BasicRootsBlock roots;
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
    }

    public AerialRootsSoilProperties getAerialRootsSoil() {
        return defaultSoil;
    }

    public BasicRootsBlock getRoots() {
        return roots;
    }

}
