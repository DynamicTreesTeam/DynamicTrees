package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public class SurfaceRootStateGenerator implements Generator<DTDataProvider.BlockState, Family> {

    public static final DependencyKey<SurfaceRootBlock> SURFACE_ROOT = new DependencyKey<>("surface_root");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");

    @Override
    public void generate(DTDataProvider.BlockState prov, Family input, Dependencies dependencies) {
        if (!(prov instanceof DTBlockStateProvider provider)) {
            return;
        }
        final SurfaceRootBlock surfaceRoot = dependencies.get(SURFACE_ROOT);
        final Identifier modelId = provider.blockModelLocation(surfaceRoot);
        final Map<String, Identifier> textures = new LinkedHashMap<>();
        textures.put("bark", input.getTexturePath(Family.BRANCH).orElse(
                provider.block(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_LOG))))
        ));
        provider.customLoaderModel(modelId, input.getSurfaceRootLoader(), textures);
        provider.simpleBlock(surfaceRoot, modelId);
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(SURFACE_ROOT, input.getSurfaceRoot())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
