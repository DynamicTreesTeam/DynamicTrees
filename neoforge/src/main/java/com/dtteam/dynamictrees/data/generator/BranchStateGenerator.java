package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class BranchStateGenerator implements Generator<DTDataProvider.BlockState, Family> {

    public static final DependencyKey<BranchBlock> BRANCH = new DependencyKey<>("branch");
    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");

    @Override
    public void generate(DTDataProvider.BlockState prov, Family input, Dependencies dependencies) {
        if (!(prov instanceof DTBlockStateProvider provider)) {
            return;
        }
        final BranchBlock branch = dependencies.get(BRANCH);
        final Identifier modelId = provider.blockModelLocation(branch);
        final Map<String, Identifier> textures = new LinkedHashMap<>();
        Block block = dependencies.get(PRIMITIVE_LOG);
        input.addBranchTextures(textures::put, provider.block(BuiltInRegistries.BLOCK.getKey(block)), block);
        provider.customLoaderModel(modelId, input.getBranchLoader(), textures);
        provider.simpleBlock(branch, modelId);
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(BRANCH, input.getBranch())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
