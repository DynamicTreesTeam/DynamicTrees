package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Max Hyper
 */
public class RootsStateGenerator implements Generator<DTDataProvider.BlockState, Family> {

    public static final DependencyKey<BranchBlock> ROOT = new DependencyKey<>("root");
    public static final DependencyKey<Block> PRIMITIVE_ROOT = new DependencyKey<>("primitive_root");
    public static final DependencyKey<Block> PRIMITIVE_FILLED_ROOT = new DependencyKey<>("filled_primitive_root");
    public static final DependencyKey<Block> PRIMITIVE_COVERED_ROOT = new DependencyKey<>("covered_primitive_root");

    @Override
    public void generate(DTDataProvider.BlockState prov, Family input, Dependencies dependencies) {
        if (!(prov instanceof DTBlockStateProvider provider)) {
            return;
        }
        final BranchBlock root = dependencies.get(ROOT);
        final Identifier exposedId = provider.blockModelLocation(root);
        final Identifier filledId = Identifier.fromNamespaceAndPath(exposedId.getNamespace(), exposedId.getPath() + "_filled");

        final Map<String, Identifier> exposedTextures = new LinkedHashMap<>();
        input.addRootTextures(exposedTextures::put, provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_ROOT))));
        provider.customLoaderModel(exposedId, input.getRootsLoader(), exposedTextures, "cutout_mipped");

        final Map<String, Identifier> filledTextures = new LinkedHashMap<>();
        input.addRootTextures(filledTextures::put, provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_FILLED_ROOT))));
        provider.customLoaderModel(filledId, input.getRootsLoader(), filledTextures);

        Identifier coveredModel = input.getModelPath(Family.COVERED_ROOTS_BLOCK).orElse(
                provider.blockTexture(dependencies.get(PRIMITIVE_COVERED_ROOT))
        );

        provider.variants(root)
                .variant(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.EXPOSED, exposedId)
                .variant(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.FILLED, filledId)
                .variant(BasicRootsBlock.LAYER, BasicRootsBlock.Layer.COVERED, coveredModel)
                .finish();
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        UndergroundRootsFamily mangroveInput = (UndergroundRootsFamily) input;
        return new Dependencies()
                .append(ROOT, mangroveInput.getRoots())
                .append(PRIMITIVE_ROOT, mangroveInput.getPrimitiveRoots())
                .append(PRIMITIVE_FILLED_ROOT, mangroveInput.getPrimitiveFilledRoots())
                .append(PRIMITIVE_COVERED_ROOT, mangroveInput.getPrimitiveCoveredRoots());
    }

}
