package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.model.blockstate.BranchBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class BranchLoaderBuilder extends CustomBlockStateModelBuilder {

    private final Map<String, Identifier> textures;

    public BranchLoaderBuilder(Map<String, Identifier> textures) {
        super();
        this.textures = textures;
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
        return new BranchBlockStateModel.Unbaked(textures.get("bark"), textures.get("rings"));
    }
}
