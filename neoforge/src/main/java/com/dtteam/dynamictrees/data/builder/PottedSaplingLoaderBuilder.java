package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.model.blockstate.PottedSaplingBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

/**
 * @author Harley O'Connor
 */
public final class PottedSaplingLoaderBuilder extends CustomBlockStateModelBuilder {

    private Identifier potModel;

    public PottedSaplingLoaderBuilder(Identifier potModel) {
        super();
        this.potModel = potModel;
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
        return new PottedSaplingBlockStateModel.Unbaked(potModel);
    }
}
