package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.builder.BranchLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

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
        Identifier primitiveLogPath = Generator.blockPath(BuiltInRegistries.BLOCK.getKey(primitiveLog));

        final Map<String, Identifier> textures = new HashMap<>();
        input.addBranchTextures(textures::put, primitiveLogPath, primitiveLog);

        BranchLoaderBuilder builder = BranchLoaderBuilder.branchBuilders.get(input.getBranchLoader())
                .apply(textures, input);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(branch, MultiVariant.of(builder)));
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(BRANCH, input.getBranch())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
