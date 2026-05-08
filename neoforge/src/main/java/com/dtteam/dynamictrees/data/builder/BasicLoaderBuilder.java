package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.event.handler.ClientModEventHandler;
import com.dtteam.dynamictrees.model.blockstate.AerialRootsSoilBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.RootsBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.SurfaceRootBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.UnbakedBranchModel;
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
                        new BasicLoaderBuilder(()-> new UnbakedBranchModel(textures.get("bark"), textures.get("rings"), Optional.ofNullable(family))));
        loaderBuilders.put(
                ClientModEventHandler.SURFACE_ROOT, (textures, _)->
                        new BasicLoaderBuilder(()-> new SurfaceRootBlockStateModel.Unbaked(textures.get("bark"))));
        loaderBuilders.put(
                ClientModEventHandler.ROOTS, (textures, _)->
                        new BasicLoaderBuilder(()-> new RootsBlockStateModel.Unbaked(textures.get("bark"), textures.get("rings"), false)));
        loaderBuilders.put(
                ClientModEventHandler.AERIAL_ROOTS_SOIL, (textures, family)->
                        new BasicLoaderBuilder(()-> new AerialRootsSoilBlockStateModel.Unbaked(
                                textures.get("end"),
                                textures.get("overlay"),
                                textures.get("overlay_end"),
                                textures.get("side"),
                                Optional.ofNullable(family))));
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
