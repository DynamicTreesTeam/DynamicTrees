package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @author Max Hyper
 */
public class RootsItemModelGenerator implements Generator<ItemModelGenerators, Family> {

    public static final DependencyKey<Item> ROOT_ITEM = new DependencyKey<>("root_item");
    public static final DependencyKey<Block> PRIMITIVE_ROOT = new DependencyKey<>("primitive_root");
    @Override
    public void generate(ItemModelGenerators generators, Family input, Dependencies dependencies) {
        generators.createFlatItemModel(dependencies.get(ROOT_ITEM), ModelTemplates.FLAT_ITEM);
//        if (prov instanceof DTItemModelProvider provider){
//            final ItemModelBuilder builder = provider.withExistingParent(
//                    String.valueOf(BuiltInRegistries.ITEM.getKey(dependencies.get(ROOT_ITEM))),
//                    input.getRootItemParentLocation()
//            );
//            input.addRootTextures(
//                    builder::texture,
//                    provider.block(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_ROOT)))
//            );
//        }
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        UndergroundRootsFamily mangroveInput = (UndergroundRootsFamily) input;
        return new Dependencies()
                .append(ROOT_ITEM, mangroveInput.getRootsItem())
                .append(PRIMITIVE_ROOT, mangroveInput.getPrimitiveRoots());
    }

}