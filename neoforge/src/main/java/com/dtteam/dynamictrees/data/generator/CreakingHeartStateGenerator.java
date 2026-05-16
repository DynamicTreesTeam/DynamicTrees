
package com.dtteam.dynamictrees.data.generator;

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
import net.minecraft.world.level.block.state.properties.CreakingHeartState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Harley O'Connor
 */
public class CreakingHeartStateGenerator extends BranchStateGenerator {

    public static final DependencyKey<Block> PRIMITIVE_HEART = new DependencyKey<>("primitive_heart");

    @Override
    public void generate(BlockModelGenerators generators, Family input, Dependencies dependencies) {
        if (input instanceof CreakingHeartFamily creakingFamily){
            final BranchBlock heartBranch = dependencies.get(BRANCH);

            BasicLoaderBuilder awakeBuilder = getBuilder(creakingFamily, dependencies, CreakingHeartState.AWAKE.toString());
            BasicLoaderBuilder dormantBuilder = getBuilder(creakingFamily, dependencies, CreakingHeartState.DORMANT.toString());
            BasicLoaderBuilder uprootedBuilder = getBuilder(creakingFamily, dependencies, "");

            var propertyDispatch = PropertyDispatch.initial(CreakingHeartBranchBlock.STATE).generate(
                    state -> switch (state){
                        case AWAKE -> MultiVariant.of(awakeBuilder);
                        case DORMANT -> MultiVariant.of(dormantBuilder);
                        case UPROOTED -> MultiVariant.of(uprootedBuilder);
                    }
            );

            generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(heartBranch).with(propertyDispatch));
        } else {
            super.generate(generators, input, dependencies);
        }
    }

    private BasicLoaderBuilder getBuilder(CreakingHeartFamily input, Dependencies dependencies, String heartState) {
        final Block primitiveHeart = dependencies.get(PRIMITIVE_HEART);
        final Block primitiveLog = dependencies.get(PRIMITIVE_LOG);
        Identifier primitiveHeartPath = ModelLocationUtils.getModelLocation(primitiveHeart);
        Identifier primitiveLogPath = ModelLocationUtils.getModelLocation(primitiveLog);
        final Map<String, Identifier> textures = new HashMap<>();
        input.addHeartTextures(textures::put, primitiveHeartPath, primitiveHeart, heartState);
        input.addBranchTextures(textures::put, primitiveLogPath, primitiveLog);
        return getBranchLoader(input).apply(textures, input);
    }

    @Override
    protected BiFunction<Map<String, Identifier>, Family, BasicLoaderBuilder> getBranchLoader(Family input) {
        if (input instanceof CreakingHeartFamily heartFamily){
            return BasicLoaderBuilder.loaderBuilders.get(heartFamily.getHeartBranchLoader());
        }
        return super.getBranchLoader(input);
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        if (input instanceof CreakingHeartFamily heartFamily){
            return new Dependencies()
                    .append(BRANCH, heartFamily.getHeartBranch())
                    .append(PRIMITIVE_HEART, heartFamily.getPrimitiveHeartLog())
                    .append(PRIMITIVE_LOG, input.getPrimitiveLog());
        }
        return new Dependencies();
    }

}
