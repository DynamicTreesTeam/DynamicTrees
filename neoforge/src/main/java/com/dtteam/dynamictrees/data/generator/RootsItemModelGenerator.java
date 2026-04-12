//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.data.DTDataProvider;
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTItemModelProvider;
//import com.dtteam.dynamictrees.tree.family.Family;
//import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.block.Block;
//import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
//
///**
// * @author Max Hyper
// */
//public class RootsItemModelGenerator implements Generator<DTDataProvider.ItemModel, Family> {
//
//    public static final DependencyKey<Item> ROOT_ITEM = new DependencyKey<>("root_item");
//    public static final DependencyKey<Block> PRIMITIVE_ROOT = new DependencyKey<>("primitive_root");
//    @Override
//    public void generate(DTDataProvider.ItemModel prov, Family input, Dependencies dependencies) {
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
//    }
//
//    @Override
//    public Dependencies gatherDependencies(Family input) {
//        UndergroundRootsFamily mangroveInput = (UndergroundRootsFamily) input;
//        return new Dependencies()
//                .append(ROOT_ITEM, mangroveInput.getRootsItem())
//                .append(PRIMITIVE_ROOT, mangroveInput.getPrimitiveRoots());
//    }
//
//}