package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.PalmBranchBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import com.dtteam.dynamictrees.tree.species.PalmSpecies;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;

public class PalmFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(PalmFamily::new);

    public PalmFamily(Identifier name) {
        super(name);
    }

    @Override
    public void setCommonSpecies(Species species) {
        super.setCommonSpecies(species);
        if (!(species instanceof PalmSpecies)) {
            LogManager.getLogger().warn("Common species {} for palm family {} is not of type {}", species.getRegistryName(), getRegistryName(), PalmSpecies.class);
        }
    }

    @Override
    public void setCommonLeaves(LeavesProperties properties) {
        super.setCommonLeaves(properties);
        if (!(properties instanceof PalmLeavesProperties)) {
            LogManager.getLogger().warn("Common leaves properties {} for palm family {} is not of type {}", properties.getRegistryName(), getRegistryName(), PalmLeavesProperties.class);
        }
    }

    @Override
    protected BranchBlock createBranchBlock(Identifier name) {
        final BasicBranchBlock branch = new PalmBranchBlock(name, this.getProperties());
        if (this.isFireProof())
            branch.setFireSpreadSpeed(0).setFlammability(0);
        return branch;
    }
}