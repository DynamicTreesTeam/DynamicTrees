package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.model.blockstate.PottedSaplingBlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public final class PottedSaplingLoaderBuilder extends InvariantLoaderBuilder {

    private Identifier potModel;

    public PottedSaplingLoaderBuilder(Identifier potModel) {
        super();
        this.potModel = potModel;
    }

    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return new PottedSaplingBlockStateModel.Unbaked(potModel);
    }
}
