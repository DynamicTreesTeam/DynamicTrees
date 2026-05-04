package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.model.blockstate.AerialRootsSoilBlockStateModel;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class AerialRootsSoilLoaderBuilder extends CustomBlockStateModelBuilder {

    public static final HashMap<Identifier, BiFunction<Map<String, Identifier>, Family, AerialRootsSoilLoaderBuilder>> branchBuilders = new HashMap<>();
    static {
        branchBuilders.put(
                DynamicTrees.location("aerial_roots_soil"), (textures, family)->
                        new AerialRootsSoilLoaderBuilder(()-> new AerialRootsSoilBlockStateModel.Unbaked(
                                textures.get("end"),
                                textures.get("overlay"),
                                textures.get("overlay_end"),
                                textures.get("side"),
                                Optional.ofNullable(family))));
    }

    private final Supplier<CustomUnbakedBlockStateModel> unbakedSupplier;

    public AerialRootsSoilLoaderBuilder(Supplier<CustomUnbakedBlockStateModel> unbakedSupplier) {
        super();
        this.unbakedSupplier = unbakedSupplier;
    }

    @Override
    public CustomBlockStateModelBuilder with(VariantMutator variantMutator) {
        return this;
    }

    @Override
    public CustomBlockStateModelBuilder with(UnbakedMutator unbakedMutator) {
        return this;
    }

    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return unbakedSupplier.get();
    }
}
