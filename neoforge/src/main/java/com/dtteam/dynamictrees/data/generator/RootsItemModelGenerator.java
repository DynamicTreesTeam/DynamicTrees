//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTItemModelProvider;
//import com.dtteam.dynamictrees.tree.family.Family;
//import com.dtteam.dynamictrees.tree.family.MangroveFamily;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.block.Block;
//import net.minecraftforge.client.model.generators.ItemModelBuilder;
//import net.minecraftforge.registries.ForgeRegistries;
//
///**
// * @author Max Hyper
// */
//public class RootsItemModelGenerator implements Generator<DTItemModelProvider, Family> {
//
//    public static final DependencyKey<Item> ROOT_ITEM = new DependencyKey<>("root_item");
//    public static final DependencyKey<Block> PRIMITIVE_ROOT = new DependencyKey<>("primitive_root");
//    @Override
//    public void generate(DTItemModelProvider provider, Family input, Dependencies dependencies) {
//        final ItemModelBuilder builder = provider.withExistingParent(
//                String.valueOf(ForgeRegistries.ITEMS.getKey(dependencies.get(ROOT_ITEM))),
//                input.getRootItemParentLocation()
//        );
//        input.addRootTextures(
//                builder::texture,
//                provider.block(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_ROOT)))
//        );
//    }
//
//    @Override
//    public Dependencies gatherDependencies(Family input) {
//        MangroveFamily mangroveInput = (MangroveFamily) input;
//        return new Dependencies()
//                .append(ROOT_ITEM, mangroveInput.getRootsItem())
//                .append(PRIMITIVE_ROOT, mangroveInput.getPrimitiveRoots());
//    }
//
//}