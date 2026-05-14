package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CreakingHeartFamily extends AltBranchFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CreakingHeartFamily::new);

    protected float heartHardnessMultiplier = 10;

    public CreakingHeartFamily(Identifier name) {
        super(name);
    }

//    public boolean hasHeart(BlockGetter level, BlockPos rootPos, BranchBlock branch){
//        final FindEndsNode endFinder = new FindEndsNode();
//        branch.analyse(level, rootPos, new MapSignal(endFinder));
//        TreeHelper.findRootNode()
//    }

    @Override
    protected BranchBlock createAltBranchBlock(Identifier name) {
        BasicBranchBlock branch = new CreakingHeartBranchBlock(name, this.getProperties());
        if (this.isFireProof()) branch.setFireSpreadSpeed(0).setFlammability(0);
        return branch;
    }

    @Override
    protected BranchBlock createBranchBlock(Identifier name) {
        final BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(name, this.getProperties()){

            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return super.getHardness(state, level, pos);
            }
        } : new BasicBranchBlock(name, this.getProperties()){
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return super.getHardness(state, level, pos);
            }
        };
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }
}
