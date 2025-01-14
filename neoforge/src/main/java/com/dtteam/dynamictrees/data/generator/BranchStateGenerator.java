//package com.dtteam.dynamictrees.data.generator;
//
//import com.dtteam.dynamictrees.block.branch.BranchBlock;
//import com.dtteam.dynamictrees.data.Generator;
//import com.dtteam.dynamictrees.data.provider.BranchLoaderBuilder;
//import com.dtteam.dynamictrees.data.provider.DTBlockStateProvider;
//import com.dtteam.dynamictrees.tree.family.Family;
//import net.minecraft.world.level.block.Block;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.Objects;
//
///**
// * @author Harley O'Connor
// */
//public class BranchStateGenerator implements Generator<DTBlockStateProvider, Family> {
//
//    public static final DependencyKey<BranchBlock> BRANCH = new DependencyKey<>("branch");
//    public static final DependencyKey<Block> PRIMITIVE_LOG = new DependencyKey<>("primitive_log");
//
//    @Override
//    public void generate(DTBlockStateProvider provider, Family input, Dependencies dependencies) {
//        final BranchBlock branch = dependencies.get(BRANCH);
//        final BranchLoaderBuilder builder = provider.models().getBuilder(
//                Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(branch)).getPath()
//        ).customLoader(branch.getFamily().getBranchLoaderConstructor());
//        Block block = dependencies.get(PRIMITIVE_LOG);
//        input.addBranchTextures(builder::texture, provider.block(ForgeRegistries.BLOCKS.getKey(block)), block);
//        provider.simpleBlock(branch, builder.end());
//    }
//
//    @Override
//    public Dependencies gatherDependencies(Family input) {
//        return new Dependencies()
//                .append(BRANCH, input.getBranch())
//                .append(PRIMITIVE_LOG, input.getPrimitiveLog());
//    }
//
//}
