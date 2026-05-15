
package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.CreakingHeartBranchState;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class CreakingHeartStateGenerator extends AltBranchStateGenerator {

    public static final DependencyKey<Block> PRIMITIVE_REGULAR_LOG = new DependencyKey<>("primitive_regular_log");

    @Override
    public void generate(BlockModelGenerators generators, Family input, Dependencies dependencies) {
        if (input instanceof CreakingHeartFamily creakingFamily){
            final BranchBlock heartBranch = dependencies.get(BRANCH);
            final Block primitiveHeart = dependencies.get(PRIMITIVE_LOG);
            final Block primitiveLog = dependencies.get(PRIMITIVE_REGULAR_LOG);
            Identifier primitiveHeartPath = ModelLocationUtils.getModelLocation(primitiveHeart);
            Identifier primitiveLogPath = ModelLocationUtils.getModelLocation(primitiveLog);

            final Map<String, Identifier> texturesAwake = new HashMap<>();
            final Map<String, Identifier> texturesDormant = new HashMap<>();
            creakingFamily.addHeartTextures(texturesAwake::put, primitiveHeartPath, primitiveHeart, CreakingHeartBranchState.AWAKE.toString());
            creakingFamily.addBranchTextures(texturesAwake::put, primitiveLogPath, primitiveLog);
            creakingFamily.addHeartTextures(texturesDormant::put, primitiveHeartPath, primitiveHeart, CreakingHeartBranchState.DORMANT.toString());
            creakingFamily.addBranchTextures(texturesDormant::put, primitiveLogPath, primitiveLog);

            BasicLoaderBuilder awakeBuilder = getBranchLoader(input).apply(texturesAwake, input);
            BasicLoaderBuilder dormantBuilder = getBranchLoader(input).apply(texturesDormant, input);

            var propertyDispatch = PropertyDispatch.initial(CreakingHeartBranchBlock.STATE).generate(
                    state -> MultiVariant.of(state.isAwake() ? awakeBuilder : dormantBuilder)
            );

            generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(heartBranch).with(propertyDispatch));
        } else {
            super.generate(generators, input, dependencies);
        }
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return super.gatherDependencies(input).append(PRIMITIVE_REGULAR_LOG, input.getPrimitiveLog());
    }

}
