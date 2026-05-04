package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Max Hyper
 */
public final class AerialRootSoilGenerator extends SoilStateGenerator {

    public static final DependencyKey<Block> PRIMITIVE_ROOTS = new DependencyKey<>("roots");

    @Override
    public void generate(BlockModelGenerators generators, SoilProperties input, Dependencies dependencies) {
        if (input instanceof AerialRootsSoilProperties aerialInput){
            generators.createTrivialCube(dependencies.get(SOIL));

//        if (prov instanceof DTBlockStateProvider provider){
//            VariantBlockStateBuilder builder = provider.getVariantBuilder(dependencies.get(SOIL));
//            for (int i=1; i<=8; i++){
//                builder = builder.partialState().with(AerialRootsSoilProperties.RootSoilBlock.RADIUS, i)
//                        .modelForState().modelFile(soilModelBuilder(
//                                provider, input, i,
//                                ModelLocationUtils.getModelLocation(dependencies.get(SOIL)).getPath(),
//                                dependencies.get(PRIMITIVE_SOIL),
//                                dependencies.get(ROOTS))
//                        ).addModel();
//            }
//        }
        }
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        AerialRootsSoilProperties aerialInput = (AerialRootsSoilProperties) input;
        return super.gatherDependencies(input)
                .append(PRIMITIVE_ROOTS, aerialInput.getFamily().getPrimitiveRoots());
    }

    private Map<String, Identifier> mapTextures(AerialRootsSoilProperties input, Block primitiveSoil, Block primitiveRoots){
        final Map<String, Identifier> textures = new HashMap<>();
        Identifier side = input.getFamily().getTexturePath(Family.BRANCH)
                .orElse(ModelLocationUtils.getModelLocation(primitiveSoil));
        Identifier bark = input.getFamily().getTexturePath(Family.BRANCH_TOP)
                .orElse(IdentifierUtils.suffix(ModelLocationUtils.getModelLocation(primitiveSoil),"_top"));
        Identifier roots_side = input.getFamily().getTexturePath(Family.ROOTS_SIDE)
                .orElse(IdentifierUtils.suffix(ModelLocationUtils.getModelLocation(primitiveRoots), "_side"));
        Identifier roots_top = input.getFamily().getTexturePath(Family.ROOTS_SIDE)
                .orElse(IdentifierUtils.suffix(ModelLocationUtils.getModelLocation(primitiveRoots),"_top"));
        Optional<Identifier> roots = input.getTexturePath(SoilProperties.ROOTS);
        textures.put("side", side);
        textures.put("end", bark);
        textures.put("overlay", roots_side);
        textures.put("overlay_end", roots_top);
        roots.ifPresent(identifier -> textures.put("roots", identifier));
        return textures;
    }

}
