package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTItemModelProvider;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class BranchItemModelGenerator implements Generator<DTDataProvider.ItemModel, Family> {

    public static final DependencyKey<Block> PRIMITIVE_LOG_BLOCK = new DependencyKey<>("primitive_log_block");
    public static final DependencyKey<Item> PRIMITIVE_LOG_ITEM = new DependencyKey<>("primitive_log_item");

    @Override
    public void generate(DTDataProvider.ItemModel prov, Family input, Dependencies dependencies) {
        if (!(prov instanceof DTItemModelProvider provider)) {
            return;
        }
        final Map<String, Identifier> textures = new LinkedHashMap<>();
        Block block = dependencies.get(PRIMITIVE_LOG_BLOCK);
        input.addBranchTextures(textures::put, provider.block(BuiltInRegistries.BLOCK.getKey(block)), block);
        provider.parentedItemModel(dependencies.get(PRIMITIVE_LOG_ITEM), input.getBranchItemParentLocation(), textures);
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(PRIMITIVE_LOG_BLOCK, input.getPrimitiveLog())
                .append(PRIMITIVE_LOG_ITEM, input.getBranchItem());
    }

}
