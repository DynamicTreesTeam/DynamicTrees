package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @author Harley O'Connor
 */
public class BranchItemModelGenerator implements Generator<ItemModelGenerators, Family> {

    public static final DependencyKey<Block> PRIMITIVE_LOG_BLOCK = new DependencyKey<>("primitive_log_block");
    public static final DependencyKey<Item> PRIMITIVE_LOG_ITEM = new DependencyKey<>("primitive_log_item");

    @Override
    public void generate(ItemModelGenerators generators, Family input, Dependencies dependencies) {
        generators.generateFlatItem(dependencies.get(PRIMITIVE_LOG_ITEM), ModelTemplates.FLAT_ITEM);
//        if (prov instanceof DTItemModelProvider provider){
//            final ItemModelBuilder builder = provider.withExistingParent(
//                    String.valueOf(BuiltInRegistries.ITEM.getKey(dependencies.get(PRIMITIVE_LOG_ITEM))),
//                    input.getBranchItemParentLocation()
//            );
//            Block block = dependencies.get(PRIMITIVE_LOG_BLOCK);
//            input.addBranchTextures(
//                    builder::texture,
//                    provider.block(BuiltInRegistries.BLOCK.getKey(block)),
//                    block
//            );
//        }
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(PRIMITIVE_LOG_BLOCK, input.getPrimitiveLog())
                .append(PRIMITIVE_LOG_ITEM, input.getBranchItem());
    }

}