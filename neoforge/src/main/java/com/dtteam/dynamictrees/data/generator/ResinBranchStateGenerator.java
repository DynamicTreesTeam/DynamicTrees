
package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResinBranchStateGenerator extends AltBranchStateGenerator {

    public static final DependencyKey<Block> RESIN_CLUMP = new DependencyKey<>("resin_clump");

    @Override
    protected void acceptOutput(BlockModelGenerators generators, Family input, Dependencies dependencies, BranchBlock branch, BasicLoaderBuilder branchBuilder) {
        if (input instanceof CreakingHeartFamily creakingHeartFamily){
            final Block resinClump = dependencies.get(RESIN_CLUMP);
            Identifier resinClumpPath = ModelLocationUtils.getModelLocation(resinClump);

            final Map<String, Identifier> resinTextures = new HashMap<>();
            creakingHeartFamily.addResinTextures(resinTextures::put, resinClumpPath);
            BasicLoaderBuilder resinBuilder = getBranchLoader(input).apply(resinTextures, input);

            generators.blockStateOutput.accept(
                    MultiPartGenerator.multiPart(branch)
                            .with(MultiVariant.of(branchBuilder))
                            .with(MultiVariant.of(resinBuilder))
            );
        } else {
            super.acceptOutput(generators, input, dependencies, branch, branchBuilder);
        }
    }
    @Override
    public Dependencies gatherDependencies(Family input) {
        if (input instanceof CreakingHeartFamily heartFamily){
            return super.gatherDependencies(input)
                    .append(RESIN_CLUMP, Optional.of(heartFamily.getResinBlock()));
        }
        return new Dependencies();
    }

}
