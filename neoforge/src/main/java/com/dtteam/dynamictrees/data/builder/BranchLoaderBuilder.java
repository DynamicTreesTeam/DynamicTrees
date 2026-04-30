package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.event.handler.ClientModEventHandler;
import com.dtteam.dynamictrees.model.blockstate.SurfaceRootBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.UnbakedBranchModel;
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
public final class BranchLoaderBuilder extends CustomBlockStateModelBuilder {

    public static final HashMap<Identifier, BiFunction<Map<String, Identifier>, Family, BranchLoaderBuilder>> branchBuilders = new HashMap<>();
    static {
        branchBuilders.put(
                ClientModEventHandler.BRANCH, (textures, family)->
                        new BranchLoaderBuilder(()-> new UnbakedBranchModel(textures.get("bark"), textures.get("rings"), Optional.of(family))));
        branchBuilders.put(
                ClientModEventHandler.SURFACE_ROOT, (textures, _)->
                        new BranchLoaderBuilder(()-> new SurfaceRootBlockStateModel.Unbaked(textures.get("bark"))));
//        branchBuilders.put(
//                ClientModEventHandler.ROOTS, (textures)->
//                        new BranchLoaderBuilder(textures, ()-> new RootsBlockStateModel.Unbaked(textures.get("bark"), textures.get("rings"))));
    }

    private final Supplier<CustomUnbakedBlockStateModel> unbakedSupplier;

    public BranchLoaderBuilder(Supplier<CustomUnbakedBlockStateModel> unbakedSupplier) {
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
