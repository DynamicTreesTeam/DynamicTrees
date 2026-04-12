//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.block.sapling.DynamicSaplingBlock;
//import com.dtteam.dynamictrees.data.DTDataProvider;
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
//import com.dtteam.dynamictrees.tree.species.Species;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.level.block.Block;
//import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
//
//import java.util.Objects;
//import java.util.Optional;
//
///**
// * @author Harley O'Connor
// */
//public class SaplingStateGenerator implements Generator<DTDataProvider.BlockState, Species> {
//
//    public static final DependencyKey<DynamicSaplingBlock> SAPLING = new DependencyKey<>("sapling");
//    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");
//    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves", true);
//
//    @Override
//    public void generate(DTDataProvider.BlockState prov, Species input, Dependencies dependencies) {
//        if (prov instanceof DTBlockStateProvider provider){
//            final Optional<Identifier> leavesTextureLocation = dependencies.getOptional(PRIMITIVE_LEAVES)
//                    .map(primitiveLeaves -> provider.block(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(primitiveLeaves))));
//            final Identifier primitiveLogLocation = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(dependencies.get(PRIMITIVE_LOG)));
//
//            final BlockModelBuilder builder = provider.models().getBuilder(input.getSaplingModelName())
//                    .parent(provider.models().getExistingFile(input.getSaplingSmartModelLocation()))
//                    .renderType("cutout_mipped");
//            input.addSaplingTextures(builder::texture, leavesTextureLocation.orElse(primitiveLogLocation), provider.block(primitiveLogLocation));
//            provider.simpleBlock(dependencies.get(SAPLING), builder);
//        }
//    }
//
//    @Override
//    public Dependencies gatherDependencies(Species input) {
//        return new Dependencies()
//                .append(SAPLING, input.getSapling())
//                .append(PRIMITIVE_LOG, input.getFamily().getPrimitiveLog())
//                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeaves());
//    }
//
//}
