package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResinBranchStateGenerator extends BranchStateGenerator {

    @Override
    public void generate(DTDataProvider.BlockState prov, Family input, Dependencies dependencies) {
        if (!(prov instanceof DTBlockStateProvider provider) || !(input instanceof CreakingHeartFamily family)) {
            return;
        }
        final BranchBlock branch = dependencies.get(BRANCH);
        final Identifier modelId = provider.blockModelLocation(branch);
        final Map<String, Identifier> textures = new LinkedHashMap<>();
        Block resinBlock = family.getResinBlock();
        family.addResinTextures(textures::put, provider.block(BuiltInRegistries.BLOCK.getKey(resinBlock)));
        provider.customLoaderModel(modelId, input.getBranchLoader(), textures);
        provider.simpleBlock(branch, modelId);
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        CreakingHeartFamily family = input instanceof CreakingHeartFamily heartFamily ? heartFamily : null;
        return new Dependencies()
                .append(BRANCH, family != null ? family.getResinBranch() : java.util.Optional.empty())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
    }

}
