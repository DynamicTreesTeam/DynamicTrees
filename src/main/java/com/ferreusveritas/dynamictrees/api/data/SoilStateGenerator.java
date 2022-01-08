package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import net.minecraft.block.Block;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public class SoilStateGenerator implements Generator<DTBlockStateProvider, SoilProperties> {

    public static final DependencyKey<RootyBlock> SOIL = new DependencyKey<>("soil");
    public static final DependencyKey<Block> PRIMITIVE_SOIL = new DependencyKey<>("primitive_soil");

    @Override
    public void generate(DTBlockStateProvider provider, SoilProperties input, Dependencies dependencies) {
        provider.getMultipartBuilder(dependencies.get(SOIL))
                .part().modelFile(provider.models().getExistingFile(
                        provider.block(Objects.requireNonNull(dependencies.get(PRIMITIVE_SOIL).getRegistryName()))
                )).addModel().end()
                .part().modelFile(provider.models().getExistingFile(input.getRootsOverlayLocation())).addModel().end();
    }

    @Override
    public boolean verifyInput(SoilProperties input) {
        return !input.hasSubstitute(); // Don't create states for substitutes as they use another soil's block.
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        return new Dependencies()
                .append(SOIL, input.getBlock())
                .append(PRIMITIVE_SOIL, input.getPrimitiveSoilBlockOptional());
    }

}
