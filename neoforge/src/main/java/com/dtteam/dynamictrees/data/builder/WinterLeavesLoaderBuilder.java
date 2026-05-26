package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.model.blockstate.WinterLeavesBlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public final class WinterLeavesLoaderBuilder extends InvariantLoaderBuilder {

    private Identifier leavesModel;
    private Identifier winterLeavesModel;

    public WinterLeavesLoaderBuilder(Identifier leavesModel, Identifier winterLeavesModel) {
        super();
        this.leavesModel = leavesModel;
        this.winterLeavesModel = winterLeavesModel;
    }

    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return new WinterLeavesBlockStateModel.Unbaked(leavesModel, winterLeavesModel);
    }
}
