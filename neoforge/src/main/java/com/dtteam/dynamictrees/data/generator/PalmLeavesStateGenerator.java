package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class PalmLeavesStateGenerator implements Generator<DTDataProvider.BlockState, LeavesProperties> {

    public static final DependencyKey<DynamicLeavesBlock> LEAVES = new DependencyKey<>("leaves");
    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves");

    @Override
    public void generate(DTDataProvider.BlockState prov, LeavesProperties input, Dependencies dependencies) {
        if (!(prov instanceof DTBlockStateProvider provider)) {
            return;
        }
        Identifier defaultFrondsTexture = provider.block(ResourceLocationUtils.suffix(input.getRegistryName(), "_frond"));
        Identifier defaultCoreTexture = provider.block(ResourceLocationUtils.suffix(input.getRegistryName(), "_base"));
        PalmLeavesProperties palmInput = (PalmLeavesProperties) input;

        Identifier frondId = Identifier.fromNamespaceAndPath(palmInput.getRegistryName().getNamespace(), palmInput.getFrondsModelName());
        Map<String, Identifier> frondTextures = new LinkedHashMap<>();
        palmInput.addFrondTextures(frondTextures::put, defaultFrondsTexture);
        provider.customLoaderModel(frondId, palmInput.getFrondLoader(), frondTextures);

        Identifier coreTopId = Identifier.fromNamespaceAndPath(palmInput.getRegistryName().getNamespace(), palmInput.getCoreTopModelName());
        Map<String, Identifier> coreTopTextures = new LinkedHashMap<>();
        palmInput.addFrondTextures(coreTopTextures::put, defaultFrondsTexture);
        provider.parentedBlockModel(coreTopId, palmInput.getCoreTopSmartModelLocation(), coreTopTextures, null);

        Identifier coreBottomId = Identifier.fromNamespaceAndPath(palmInput.getRegistryName().getNamespace(), palmInput.getCoreBottomModelName());
        Map<String, Identifier> coreBottomTextures = new LinkedHashMap<>();
        palmInput.addCoreTextures(coreBottomTextures::put, defaultCoreTexture);
        provider.parentedBlockModel(coreBottomId, palmInput.getCoreBottomSmartModelLocation(), coreBottomTextures, null);

        final Identifier blockModel = palmInput.getModelPath(LeavesProperties.LEAVES).orElse(
                provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_LEAVES)))
        );

        final IntegerProperty distance = LeavesBlock.DISTANCE;
        final IntegerProperty direction = PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION;
        provider.multipart(dependencies.get(LEAVES))
                .part(frondId).condition(distance, 1, 2).end()
                .part(coreTopId).condition(distance, 3).end()
                .part(coreBottomId).condition(distance, 4).end()
                .part(blockModel).useOr()
                .nestedGroup().condition(direction, 0).condition(distance, 1, 2).end()
                .nestedGroup().condition(distance, 5, 6, 7).end()
                .end()
                .finish();
    }

    @Override
    public Dependencies gatherDependencies(LeavesProperties input) {
        return new Dependencies()
                .append(LEAVES, input.getDynamicLeavesBlock())
                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeavesBlock());
    }

}
