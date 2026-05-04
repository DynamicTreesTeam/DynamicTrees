package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.fruit.FruitBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;

/**
 * @author Harley O'Connor
 */
public class FruitsStateGenerator implements Generator<BlockModelGenerators, Fruit> {

    public static final DependencyKey<FruitBlock> FRUIT_BLOCK = new DependencyKey<>("fruit");
    @Override
    public void generate(BlockModelGenerators generators, Fruit input, Dependencies dependencies) {
        FruitBlock fruitBlock = dependencies.get(FRUIT_BLOCK);

        var propertyDispatch = PropertyDispatch.initial(input.getAgeProperty()).generate(
                age -> {
                    if (age > 0 && input.rotateModel()){
                        return BlockModelGenerators.variants(
                                new Variant(modelLocation(age, input)),
                                rotatedVariant(modelLocation(age, input), Quadrant.R90),
                                rotatedVariant(modelLocation(age, input), Quadrant.R180),
                                rotatedVariant(modelLocation(age, input), Quadrant.R270)
                        );
                    } else {
                        return BlockModelGenerators.variant(new Variant(modelLocation(age, input)));
                    }
                }
        );

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(fruitBlock).with(propertyDispatch)
        );
    }

    private static Variant rotatedVariant(Identifier modelLocation, Quadrant yRot){
        return new Variant(modelLocation, Variant.SimpleModelState.DEFAULT.withY(yRot));
    }

    protected Identifier modelLocation(int age, Fruit fruit){
        return fruit.getRegistryName().withPrefix("block/fruit/").withSuffix("_age"+age);
    }

    @Override
    public Dependencies gatherDependencies(Fruit input) {
        return new Dependencies()
                .appendValue(FRUIT_BLOCK, input.getBlock());
    }

}
