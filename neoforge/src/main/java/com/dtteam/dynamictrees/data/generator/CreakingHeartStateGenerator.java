
package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CreakingHeartBlock;
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

            final Block primitiveLog = dependencies.get(PRIMITIVE_LOG);
            Identifier primitiveLogPath = ModelLocationUtils.getModelLocation(primitiveLog);

            final Map<String, Identifier> logTextures = new HashMap<>();
            input.addBranchTextures(logTextures::put, primitiveLogPath, primitiveLog);

            BasicLoaderBuilder awakeBuilder = getBuilder(creakingFamily, dependencies, logTextures, CreakingHeartState.AWAKE.toString());
            BasicLoaderBuilder dormantBuilder = getBuilder(creakingFamily, dependencies, logTextures, CreakingHeartState.DORMANT.toString());
            BasicLoaderBuilder uprootedBuilder = getBuilder(creakingFamily, dependencies, logTextures, "");
            BasicLoaderBuilder hiddenBuilder = BasicLoaderBuilder.loaderBuilders.get(input.getBranchLoader()).apply(logTextures, input);

            generators.blockStateOutput.accept(
                    MultiPartGenerator.multiPart(heartBranch)
                            .with(new ConditionBuilder().term(CreakingHeartBranchBlock.HIDDEN, true),
                                    MultiVariant.of(hiddenBuilder))
                            .with(new ConditionBuilder().term(CreakingHeartBranchBlock.HIDDEN, false)
                                            .term(CreakingHeartBranchBlock.STATE, CreakingHeartState.AWAKE),
                                    MultiVariant.of(awakeBuilder))
                            .with(new ConditionBuilder().term(CreakingHeartBranchBlock.HIDDEN, false)
                                            .term(CreakingHeartBranchBlock.STATE, CreakingHeartState.DORMANT),
                                    MultiVariant.of(dormantBuilder))
                            .with(new ConditionBuilder().term(CreakingHeartBranchBlock.HIDDEN, false)
                                            .term(CreakingHeartBranchBlock.STATE, CreakingHeartState.UPROOTED),
                                    MultiVariant.of(uprootedBuilder))
            );
        } else {
            super.generate(generators, input, dependencies);
        }
    }

    private BasicLoaderBuilder getBuilder(CreakingHeartFamily input, Dependencies dependencies, Map<String, Identifier> logTextures, String heartState) {
        final Block primitiveHeart = dependencies.get(PRIMITIVE_HEART);
        Identifier primitiveHeartPath = ModelLocationUtils.getModelLocation(primitiveHeart);
        final Map<String, Identifier> textures = new HashMap<>();
        input.addHeartTextures(textures::put, primitiveHeartPath, primitiveHeart, heartState);
        textures.putAll(logTextures);
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
