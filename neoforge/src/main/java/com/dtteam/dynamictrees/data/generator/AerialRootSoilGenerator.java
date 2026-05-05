package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.event.handler.ClientModEventHandler;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Max Hyper
 */
public final class AerialRootSoilGenerator extends SoilStateGenerator {

    public static final DependencyKey<Block> PRIMITIVE_ROOTS = new DependencyKey<>("roots");

    @Override
    public void generate(BlockModelGenerators generators, SoilProperties input, Dependencies dependencies) {
        if (input instanceof AerialRootsSoilProperties aerialInput){
            final SoilBlock soil = dependencies.get(SOIL);
            final Block primitiveSoil = dependencies.get(PRIMITIVE_SOIL);
            final Block primitiveRoots = dependencies.get(PRIMITIVE_ROOTS);
            Identifier primitiveSoilPath = ModelLocationUtils.getModelLocation(primitiveSoil);
            Identifier primitiveRootsPath = ModelLocationUtils.getModelLocation(primitiveRoots);
            Family family = aerialInput.getFamily();

            final Map<String, Identifier> textures = mapTextures(family, aerialInput, primitiveSoilPath, primitiveRootsPath);

            BasicLoaderBuilder builder = BasicLoaderBuilder.loaderBuilders.get(ClientModEventHandler.AERIAL_ROOTS_SOIL)
                    .apply(textures, family);

            generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(soil, MultiVariant.of(builder)));
        }
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        AerialRootsSoilProperties aerialInput = (AerialRootsSoilProperties) input;
        return super.gatherDependencies(input)
                .append(PRIMITIVE_ROOTS, aerialInput.getFamily().getPrimitiveRoots());
    }

    private Map<String, Identifier> mapTextures(Family family, AerialRootsSoilProperties soil, Identifier primitiveSoil, Identifier primitiveRoots){
        final Map<String, Identifier> textures = new HashMap<>();
        Identifier side = family.getTexturePath(Family.BRANCH).orElse(primitiveSoil);
        Identifier bark = family.getTexturePath(Family.BRANCH_TOP).orElse(IdentifierUtils.suffix(primitiveSoil,"_top"));
        Identifier roots_side = family.getTexturePath(Family.ROOTS_SIDE).orElse(IdentifierUtils.suffix(primitiveRoots, "_side"));
        Identifier roots_top = family.getTexturePath(Family.ROOTS_TOP).orElse(IdentifierUtils.suffix(primitiveRoots,"_top"));
        //Optional<Identifier> roots_bottom = soil.getTexturePath(SoilProperties.ROOTS);
        textures.put("side", side);
        textures.put("end", bark);
        textures.put("overlay", roots_side);
        textures.put("overlay_end", roots_top);
        //roots_bottom.ifPresent(id -> textures.put("roots", id));
        return textures;
    }

}
