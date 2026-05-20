package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.event.handler.ClientModEventHandler;
import com.dtteam.dynamictrees.model.blockstate.*;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class BasicLoaderBuilder extends InvariantLoaderBuilder {

    public static final HashMap<Identifier, BiFunction<Map<String, Identifier>, Family, BasicLoaderBuilder>> loaderBuilders = new HashMap<>();
    static {
        loaderBuilders.put(
                ClientModEventHandler.BRANCH, (textures, family)->
                        new BasicLoaderBuilder(()-> new UnbakedBranchModel(
                                textures.get("bark"),
                                textures.get("rings"),
                                Optional.ofNullable(family))));
        loaderBuilders.put(
                ClientModEventHandler.SURFACE_ROOT, (textures, _)->
                        new BasicLoaderBuilder(()-> new SurfaceRootBlockStateModel.Unbaked(
                                textures.get("bark"))));
        loaderBuilders.put(
                ClientModEventHandler.ROOTS, (textures, _)->
                        new BasicLoaderBuilder(()-> new UnbakedRootsModel(
                                textures.get("bark"),
                                textures.get("rings"),
                                false)));
        loaderBuilders.put(
                ClientModEventHandler.ROOTS.withSuffix("_opaque"), (textures, _)->
                        new BasicLoaderBuilder(()-> new UnbakedRootsModel(
                                textures.get("bark"),
                                textures.get("rings"),
                                true)));
        loaderBuilders.put(
                ClientModEventHandler.AERIAL_ROOTS_SOIL, (textures, family)->
                        new BasicLoaderBuilder(()-> new AerialRootsSoilBlockStateModel.Unbaked(
                                textures.get("end"),
                                textures.get("overlay"),
                                textures.get("overlay_end"),
                                textures.get("side"),
                                Optional.ofNullable(family))));
        loaderBuilders.put(
                ClientModEventHandler.CREAKING_HEART, (textures, family)->
                        new BasicLoaderBuilder(()-> new UnbakedCreakingHeartModel(
                                textures.get("heart_bark"),
                                textures.get("heart_rings"),
                                textures.get("bark"),
                                Optional.ofNullable(family))));
        loaderBuilders.put(
                ClientModEventHandler.ROOTS_MOSS, (textures, _)->
                        new BasicLoaderBuilder(()-> new UnbakedRootsMossModel(
                                textures.get("moss"))));
    }

    private final Supplier<CustomUnbakedBlockStateModel> unbakedSupplier;

    public BasicLoaderBuilder(Supplier<CustomUnbakedBlockStateModel> unbakedSupplier) {
        super();
        this.unbakedSupplier = unbakedSupplier;
    }

    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return unbakedSupplier.get();
    }
}
