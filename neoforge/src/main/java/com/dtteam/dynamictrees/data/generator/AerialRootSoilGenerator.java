package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

/**
 * @author Max Hyper
 */
public final class AerialRootSoilGenerator extends SoilStateGenerator {

    public static final DependencyKey<Block> ROOTS = new DependencyKey<>("roots");
    @Override
    public void generate(DTDataProvider.BlockState prov, SoilProperties input, Dependencies dependencies) {
        if (prov instanceof DTBlockStateProvider provider){
            VariantBlockStateBuilder builder = provider.getVariantBuilder(dependencies.get(SOIL));
            for (int i=1; i<=8; i++){
                builder = builder.partialState().with(AerialRootsSoilProperties.RootSoilBlock.RADIUS, i)
                        .modelForState().modelFile(soilModelBuilder(
                                provider, input, i,
                                provider.blockTexture(dependencies.get(SOIL)).getPath(),
                                dependencies.get(PRIMITIVE_SOIL),
                                dependencies.get(ROOTS))
                        ).addModel();
            }
        }
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        AerialRootsSoilProperties aerialInput = (AerialRootsSoilProperties) input;
        return new Dependencies()
                .append(SOIL, input.getBlock())
                .append(PRIMITIVE_SOIL, input.getPrimitiveSoilBlockOptional())
                .append(ROOTS, aerialInput.getFamily().getPrimitiveRoots());
    }

    private BlockModelBuilder soilModelBuilder(BlockStateProvider provider, SoilProperties input, int radius, String name, Block primitiveBlock, Block roots) {
        AerialRootsSoilProperties aerialInput = (AerialRootsSoilProperties)input;
        ResourceLocation side = aerialInput.getFamily().getTexturePath(Family.BRANCH).orElse(provider.blockTexture(primitiveBlock));
        ResourceLocation top = aerialInput.getFamily().getTexturePath(Family.BRANCH_TOP).orElse(ResourceLocationUtils.suffix(provider.blockTexture(primitiveBlock),"_top"));
        ResourceLocation roots_side = aerialInput.getFamily().getTexturePath(Family.ROOTS_SIDE).orElse(ResourceLocationUtils.suffix(provider.blockTexture(roots), "_side"));
        ResourceLocation roots_top = aerialInput.getFamily().getTexturePath(Family.ROOTS_SIDE).orElse(ResourceLocationUtils.suffix(provider.blockTexture(roots),"_top"));
        BlockModelBuilder builder = provider.models().withExistingParent(name+"_radius"+radius,  DynamicTrees.location("block/smartmodel/rooty/aerial_roots_radius"+ radius))
                .texture("side", side)
                .texture("end", top)
                .texture("overlay", roots_side)
                .texture("overlay_end", roots_top);
        input.getTexturePath(SoilProperties.ROOTS).ifPresent((r)->builder.texture("roots", r));
        return builder;
    }

}
