package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.tree.family.AltBranchFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class CreakingHeartBranchBlock extends ThickBranchBlock {

    public CreakingHeartBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
    }

    @Override
    public Optional<Block> getPrimitiveLog() {
        if (getFamily() instanceof AltBranchFamily altLogFamily)
            return altLogFamily.getPrimitiveAltLog();
        return super.getPrimitiveLog();
    }

}
