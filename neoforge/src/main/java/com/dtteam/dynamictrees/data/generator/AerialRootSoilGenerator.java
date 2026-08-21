package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Max Hyper
 */
public final class AerialRootSoilGenerator extends SoilStateGenerator {

    public static final DependencyKey<Block> ROOTS = new DependencyKey<>("roots");

    @Override
    public void generate(DTDataProvider.BlockState prov, SoilProperties input, Dependencies dependencies) {
        if (!(prov instanceof DTBlockStateProvider provider)) {
            return;
        }
        AerialRootsSoilProperties aerialInput = (AerialRootsSoilProperties) input;
        Block soil = dependencies.get(SOIL);
        Identifier soilTexture = provider.blockTexture(soil);
        DTBlockStateProvider.VariantBuilder variants = provider.variants(soil);
        for (int i = 1; i <= 8; i++) {
            Identifier modelId = Identifier.fromNamespaceAndPath(soilTexture.getNamespace(), soilTexture.getPath() + "_radius" + i);
            variants.variant(AerialRootsSoilProperties.RootSoilBlock.RADIUS, i, modelId);
            provider.parentedBlockModel(modelId,
                    DynamicTrees.location("block/smartmodel/rooty/aerial_roots_radius" + i),
                    soilTextures(provider, aerialInput, dependencies),
                    null);
        }
        variants.finish();
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        AerialRootsSoilProperties aerialInput = (AerialRootsSoilProperties) input;
        return new Dependencies()
                .append(SOIL, input.getBlock())
                .append(PRIMITIVE_SOIL, input.getPrimitiveSoilBlockOptional())
                .append(ROOTS, aerialInput.getFamily().getPrimitiveRoots());
    }

    private static Map<String, Identifier> soilTextures(DTBlockStateProvider provider, AerialRootsSoilProperties aerialInput,
                                                        Dependencies dependencies) {
        Block primitiveBlock = dependencies.get(PRIMITIVE_SOIL);
        Block roots = dependencies.get(ROOTS);
        Identifier side = aerialInput.getFamily().getTexturePath(Family.BRANCH).orElse(provider.blockTexture(primitiveBlock));
        Identifier top = aerialInput.getFamily().getTexturePath(Family.BRANCH_TOP)
                .orElse(ResourceLocationUtils.suffix(provider.blockTexture(primitiveBlock), "_top"));
        Identifier rootsSide = aerialInput.getFamily().getTexturePath(Family.ROOTS_SIDE)
                .orElse(ResourceLocationUtils.suffix(provider.blockTexture(roots), "_side"));
        Identifier rootsTop = aerialInput.getFamily().getTexturePath(Family.ROOTS_SIDE)
                .orElse(ResourceLocationUtils.suffix(provider.blockTexture(roots), "_top"));
        Map<String, Identifier> textures = new LinkedHashMap<>();
        textures.put("side", side);
        textures.put("end", top);
        textures.put("overlay", rootsSide);
        textures.put("overlay_end", rootsTop);
        aerialInput.getTexturePath(SoilProperties.ROOTS).ifPresent(r -> textures.put("roots", r));
        return textures;
    }

}
