package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.BasicRootsBlock;
import com.ferreusveritas.dynamictrees.data.provider.BranchLoaderBuilder;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * @author Max Hyper
 */
public class RootsStateGenerator implements Generator<DTBlockStateProvider, Family> {

    public static final DependencyKey<BranchBlock> ROOT = new DependencyKey<>("root");
    public static final DependencyKey<Block> PRIMITIVE_ROOT = new DependencyKey<>("primitive_root");
    public static final DependencyKey<Block> PRIMITIVE_FILLED_ROOT = new DependencyKey<>("filled_primitive_root");
    public static final DependencyKey<Block> PRIMITIVE_COVERED_ROOT = new DependencyKey<>("covered_primitive_root");

    @Override
    public void generate(DTBlockStateProvider provider, Family input, Dependencies dependencies) {
        final BranchBlock root = dependencies.get(ROOT);
        final BranchLoaderBuilder builderExposed = provider.models().getBuilder(
                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(root)).getPath()
        ).customLoader(BranchLoaderBuilder::Roots);
        input.addRootTextures(builderExposed::texture, provider.block(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_ROOT))));

        final BranchLoaderBuilder builderFilled = provider.models().getBuilder(
                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(root)).getPath() + "_filled"
        ).customLoader(BranchLoaderBuilder::Roots);
        input.addRootTextures(builderFilled::texture, provider.block(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_FILLED_ROOT))));

        provider.getVariantBuilder(root)
                .partialState().with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.EXPOSED)
                .modelForState().modelFile(builderExposed.end().renderType("cutout_mipped")).addModel()
                .partialState().with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.FILLED)
                .modelForState().modelFile(builderFilled.end()).addModel()
                .partialState().with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.COVERED)
                .modelForState().modelFile(provider.models().getExistingFile(provider.blockTexture(dependencies.get(PRIMITIVE_COVERED_ROOT)))).addModel();
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        MangroveFamily mangroveInput = (MangroveFamily) input;
        return new Dependencies()
                .append(ROOT, mangroveInput.getRoot())
                .append(PRIMITIVE_ROOT, mangroveInput.getPrimitiveRoots())
                .append(PRIMITIVE_FILLED_ROOT, mangroveInput.getPrimitiveFilledRoots())
                .append(PRIMITIVE_COVERED_ROOT, mangroveInput.getPrimitiveCoveredRoots());
    }

}
