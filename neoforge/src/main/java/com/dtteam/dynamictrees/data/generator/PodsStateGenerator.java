package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.block.pod.PodBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/**
 * @author Harley O'Connor
 */
public class PodsStateGenerator implements Generator<BlockModelGenerators, Pod> {

    public static final DependencyKey<PodBlock> POD_BLOCK = new DependencyKey<>("pod");
    @Override
    public void generate(BlockModelGenerators generators, Pod input, Dependencies dependencies) {
        PodBlock podBlock = dependencies.get(POD_BLOCK);

        PropertyDispatch<MultiVariant> propertyDispatch;
        if (input.hasVariableOffset()){
            propertyDispatch = PropertyDispatch.initial(input.getAgeProperty(), PodBlock.FACING, input.getOffsetProperty()).generate(
                    (age, facing, offset) -> BlockModelGenerators.variant(
                            new Variant(modelLocation(input, age, offset), rotateForDirection(facing))
                    )
            );
        } else {
            propertyDispatch = PropertyDispatch.initial(input.getAgeProperty(), PodBlock.FACING).generate(
                    (age, facing) -> BlockModelGenerators.variant(
                            new Variant(modelLocation(input, age), rotateForDirection(facing))
                    )
            );
        }

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(podBlock).with(propertyDispatch)
        );
    }

    protected Identifier modelLocation(Pod fruit, int age, int radius){
        return modelLocation(fruit, age).withSuffix("_radius"+radius);
    }

    protected Identifier modelLocation(Pod fruit, int age){
        return fruit.getRegistryName().withPrefix("block/pod/").withSuffix("_stage"+age);
    }

    protected Variant.SimpleModelState rotateForDirection(Direction dir){
        Quadrant quad = switch (dir){
            case WEST -> Quadrant.R90;
            case NORTH -> Quadrant.R180;
            case EAST -> Quadrant.R270;
            default -> Quadrant.R0;
        };
        return Variant.SimpleModelState.DEFAULT.withY(quad);
    }

    @Override
    public Dependencies gatherDependencies(Pod input) {
        return new Dependencies()
                .appendValue(POD_BLOCK, input.getBlock());
    }

}
