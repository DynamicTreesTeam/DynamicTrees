package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.builder.BranchLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class SurfaceRootStateGenerator implements Generator<BlockModelGenerators, Family> {

    public static final DependencyKey<SurfaceRootBlock> SURFACE_ROOT = new DependencyKey<>("surface_root");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");

    @Override
    public void generate(BlockModelGenerators generators, Family input, Dependencies dependencies) {
        final SurfaceRootBlock branch = dependencies.get(SURFACE_ROOT);

        final Map<String, Identifier> textures = new HashMap<>();
        textures.put("bark", input.getTexturePath(Family.BRANCH)
                        .orElse(ModelLocationUtils.getModelLocation(dependencies.get(PRIMITIVE_LOG))));

        BranchLoaderBuilder builder = BranchLoaderBuilder.branchBuilders.get(input.getSurfaceRootLoader())
                .apply(textures, input);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(branch, MultiVariant.of(builder)));
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(SURFACE_ROOT, input.getSurfaceRoot())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
