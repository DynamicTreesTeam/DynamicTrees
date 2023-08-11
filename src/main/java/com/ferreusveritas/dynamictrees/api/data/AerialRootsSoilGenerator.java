package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.block.branch.roots.BasicRootsBlock;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public final class AerialRootsSoilGenerator extends SoilStateGenerator {
    @Override
    public void generate(DTBlockStateProvider provider, SoilProperties input, Dependencies dependencies) {
        VariantBlockStateBuilder builder = provider.getVariantBuilder(dependencies.get(SOIL));
        for (int i=1; i<=8; i++){
            builder = builder.partialState().with(BasicRootsBlock.RADIUS, i)
                    .modelForState().modelFile(soilModelBuilder(
                            provider, i,
                            provider.blockTexture(dependencies.get(SOIL)).getPath(),
                            dependencies.get(PRIMITIVE_SOIL))
                    ).addModel();
        }
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        return new Dependencies()
                .append(SOIL, input.getBlock())
                .append(PRIMITIVE_SOIL, input.getPrimitiveSoilBlockOptional());
    }

    private BlockModelBuilder soilModelBuilder (BlockStateProvider provider, int radius, String name, Block primitiveBlock){
        ResourceLocation side = provider.blockTexture(primitiveBlock);
        return provider.models().withExistingParent(name+"_radius"+radius,  DynamicTrees.location("block/smartmodel/rooty/aerial_roots_radius"+ radius))
                .texture("side", side)
                .texture("end", side);
    }


//    builder = builder.partialState().with(BasicRootsBlock.RADIUS, i)
//                    .with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.EXPOSED)
//                    .modelForState().modelFile(exposedModel).addModel();
//    builder = builder.partialState().with(BasicRootsBlock.RADIUS, i)
//                    .with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.WATERLOGGED)
//                    .modelForState().modelFile(exposedModel).addModel();
//
//    builder = builder.partialState().with(BasicRootsBlock.RADIUS, i)
//                    .with(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.FILLED)
//                    .modelForState().modelFile(soilModelBuilder(provider, i, dependencies.get(FILLED_PRIMITIVE_ROOT))).addModel();
}
