package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.fruit.FruitBlock;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.data.models.BlockModelGenerators;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
public class FruitsStateGenerator implements Generator<BlockModelGenerators, Species> {

    public static final DependencyKey<Set<FruitBlock>> FRUIT_BLOCKS = new DependencyKey<>("fruits");
    @Override
    public void generate(BlockModelGenerators generators, Species input, Dependencies dependencies) {
        dependencies.get(FRUIT_BLOCKS).forEach(generators::createTrivialCube);
    }

    @Override
    public Dependencies gatherDependencies(Species input) {
        return new Dependencies()
                .append(FRUIT_BLOCKS, Optional.of(input.getFruits().stream().map(Fruit::getBlock).collect(Collectors.toSet())));
    }

}
