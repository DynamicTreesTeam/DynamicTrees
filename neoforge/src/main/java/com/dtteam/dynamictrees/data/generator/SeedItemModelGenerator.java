package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public class SeedItemModelGenerator implements Generator<ItemModelGenerators, Species> {

    public static final DependencyKey<Seed> SEED_ITEM = new DependencyKey<>("seed");

    @Override
    public void generate(ItemModelGenerators generators, Species input, Dependencies dependencies) {
        Identifier parentLocation = input.getSeedParentModelLocation();
        ModelTemplate seedTemplate = new ModelTemplate(Optional.of(parentLocation), Optional.empty(), TextureSlot.LAYER0);
        Item seedItem = dependencies.get(SEED_ITEM);
        Identifier textureLocation = input.getTexturePath(Species.SEED)
                .orElse(BuiltInRegistries.ITEM.getKey(seedItem))
                .withPrefix("item/");

        Identifier modelLocation = createModel(generators, seedTemplate, textureLocation);

        generators.itemModelOutput.accept(seedItem,
                ItemModelUtils.plainModel(modelLocation)
        );
    }

    private static @NotNull Identifier createModel(ItemModelGenerators generators, ModelTemplate seedTemplate, Identifier textureLocation) {
        return seedTemplate.create(
                textureLocation,
                new TextureMapping().put(TextureSlot.LAYER0, new Material(textureLocation)),
                generators.modelOutput
        );
    }

    @Override
    public Dependencies gatherDependencies(Species input) {
        return new Dependencies()
                .append(SEED_ITEM, input.getSeed());
    }

}
