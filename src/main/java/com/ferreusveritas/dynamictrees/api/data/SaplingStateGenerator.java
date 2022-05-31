package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public class SaplingStateGenerator implements Generator<DTBlockStateProvider, Species> {

    public static final DependencyKey<DynamicSaplingBlock> SAPLING = new DependencyKey<>("sapling");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");
    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves", true);

    @Override
    public void generate(DTBlockStateProvider provider, Species input, Dependencies dependencies) {
        final Optional<ResourceLocation> leavesTextureLocation = dependencies.getOptional(PRIMITIVE_LEAVES)
                .map(primitiveLeaves -> provider.block(Objects.requireNonNull(primitiveLeaves.getRegistryName())));
        final ResourceLocation primitiveLogLocation = Objects.requireNonNull(
                dependencies.get(PRIMITIVE_LOG).getRegistryName()
        );

        final BlockModelBuilder builder = provider.models().getBuilder(
                "block/saplings/" + input.getRegistryName().getPath()
        ).parent(provider.models().getExistingFile(input.getSaplingSmartModelLocation()));
        input.addSaplingTextures(builder::texture, leavesTextureLocation.orElse(primitiveLogLocation),
                provider.block(primitiveLogLocation));
        provider.simpleBlock(dependencies.get(SAPLING), builder);
    }

    @Override
    public Dependencies gatherDependencies(Species input) {
        return new Dependencies()
                .append(SAPLING, input.getSapling())
                .append(PRIMITIVE_LOG, input.getFamily().getPrimitiveLog())
                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeaves());
    }

}
