package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.block.branch.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.data.provider.BranchLoaderBuilder;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public class SurfaceRootStateGenerator implements Generator<DTBlockStateProvider, Family> {

    public static final DependencyKey<SurfaceRootBlock> SURFACE_ROOT = new DependencyKey<>("surface_root");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");

    @Override
    public void generate(DTBlockStateProvider provider, Family input, Dependencies dependencies) {
        final SurfaceRootBlock surfaceRoot = dependencies.get(SURFACE_ROOT);
        provider.simpleBlock(surfaceRoot,
                provider.models().getBuilder(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(surfaceRoot)).getPath())
                        .customLoader(BranchLoaderBuilder::surfaceRoot)
                        .texture("bark", provider.block(
                                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_LOG)))
                        )).end()
        );
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(SURFACE_ROOT, input.getSurfaceRoot())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
