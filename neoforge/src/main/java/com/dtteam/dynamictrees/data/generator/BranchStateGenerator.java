package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Harley O'Connor
 */
public class BranchStateGenerator implements Generator<BlockModelGenerators, Family> {

    public static final DependencyKey<BranchBlock> BRANCH = new DependencyKey<>("branch");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");

    @Override
    public void generate(BlockModelGenerators generators, Family input, Dependencies dependencies) {
        final BranchBlock branch = dependencies.get(BRANCH);
        final Block primitiveLog = dependencies.get(PRIMITIVE_LOG);
        Identifier primitiveLogPath = ModelLocationUtils.getModelLocation(primitiveLog);

        final Map<String, Identifier> textures = new HashMap<>();
        addTextures(input, textures, primitiveLogPath, primitiveLog);

        BasicLoaderBuilder builder = getBranchLoader(input).apply(textures, input);

        acceptOutput(generators, input, dependencies, branch, builder);
    }

    protected void acceptOutput(BlockModelGenerators generators, Family input, Dependencies dependencies, BranchBlock branch, BasicLoaderBuilder branchBuilder) {
        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(branch, MultiVariant.of(branchBuilder)));
    }

    protected void addTextures(Family input, Map<String, Identifier> textures, Identifier primitiveLogPath, Block primitiveLog) {
        input.addBranchTextures(textures::put, primitiveLogPath, primitiveLog);
    }

    protected BiFunction<Map<String, Identifier>, Family, BasicLoaderBuilder> getBranchLoader(Family input) {
        return BasicLoaderBuilder.loaderBuilders.get(input.getBranchLoader());
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(BRANCH, input.getBranch())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
