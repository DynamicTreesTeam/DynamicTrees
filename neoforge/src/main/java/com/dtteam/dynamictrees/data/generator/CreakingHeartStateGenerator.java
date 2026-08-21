package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;

import java.util.LinkedHashMap;
import java.util.Map;

public class CreakingHeartStateGenerator extends BranchStateGenerator {

    public static final DependencyKey<Block> PRIMITIVE_HEART = new DependencyKey<>("primitive_heart");

    @Override
    public void generate(DTDataProvider.BlockState prov, Family input, Dependencies dependencies) {
        if (!(prov instanceof DTBlockStateProvider provider) || !(input instanceof CreakingHeartFamily family)) {
            return;
        }
        final BranchBlock heart = dependencies.get(BRANCH);
        final Block primitiveLog = dependencies.get(PRIMITIVE_LOG);
        final Map<String, Identifier> logTextures = new LinkedHashMap<>();
        input.addBranchTextures(logTextures::put, provider.block(BuiltInRegistries.BLOCK.getKey(primitiveLog)), primitiveLog);

        Identifier hiddenModel = provider.blockModelLocation(heart).withSuffix("_hidden");
        provider.customLoaderModel(hiddenModel, input.getBranchLoader(), logTextures);

        Identifier awakeModel = modelForHeart(provider, family, heart, dependencies, logTextures, CreakingHeartState.AWAKE.toString());
        Identifier dormantModel = modelForHeart(provider, family, heart, dependencies, logTextures, CreakingHeartState.DORMANT.toString());
        Identifier uprootedModel = modelForHeart(provider, family, heart, dependencies, logTextures, "");

        provider.multipart(heart)
                .part(hiddenModel).condition(CreakingHeartBranchBlock.HIDDEN, true).end()
                .part(awakeModel).condition(CreakingHeartBranchBlock.HIDDEN, false).condition(CreakingHeartBranchBlock.STATE, CreakingHeartState.AWAKE).end()
                .part(dormantModel).condition(CreakingHeartBranchBlock.HIDDEN, false).condition(CreakingHeartBranchBlock.STATE, CreakingHeartState.DORMANT).end()
                .part(uprootedModel).condition(CreakingHeartBranchBlock.HIDDEN, false).condition(CreakingHeartBranchBlock.STATE, CreakingHeartState.UPROOTED).end()
                .finish();
    }

    private Identifier modelForHeart(DTBlockStateProvider provider, CreakingHeartFamily family, BranchBlock heart,
                                     Dependencies dependencies, Map<String, Identifier> logTextures, String state) {
        Identifier modelId = provider.blockModelLocation(heart).withSuffix(state.isEmpty() ? "" : "_" + state);
        Map<String, Identifier> textures = new LinkedHashMap<>(logTextures);
        Block primitiveHeart = dependencies.get(PRIMITIVE_HEART);
        family.addHeartTextures(textures::put, provider.block(BuiltInRegistries.BLOCK.getKey(primitiveHeart)), state);
        provider.customLoaderModel(modelId, family.getBranchLoader(), textures);
        return modelId;
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        CreakingHeartFamily family = input instanceof CreakingHeartFamily heartFamily ? heartFamily : null;
        return new Dependencies()
                .append(BRANCH, family != null ? family.getHeartBranch() : java.util.Optional.empty())
                .append(PRIMITIVE_LOG, input.getPrimitiveLog())
                .append(PRIMITIVE_HEART, family != null ? family.getPrimitiveHeartLog() : java.util.Optional.empty());
    }

}
