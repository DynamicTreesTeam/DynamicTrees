package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.data.provider.BranchLoaderBuilder;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public class BranchStateGenerator implements Generator<DTBlockStateProvider, Family> {

    public static final DependencyKey<BranchBlock> BRANCH = new DependencyKey<>("branch");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");

    @Override
    public void generate(DTBlockStateProvider provider, Family input, Dependencies dependencies) {
        final BranchBlock branch = dependencies.get(BRANCH);
        final BranchLoaderBuilder builder = provider.models().getBuilder(
                Objects.requireNonNull(branch.getRegistryName()).getPath()
        ).customLoader(branch.getFamily().getBranchLoaderConstructor());
        input.addBranchTextures(builder::texture, provider.block(dependencies.get(PRIMITIVE_LOG).getRegistryName()));
        provider.simpleBlock(branch, builder.end());
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(BRANCH, input.getBranch())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
