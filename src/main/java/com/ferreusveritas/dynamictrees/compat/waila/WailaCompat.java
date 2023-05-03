package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.block.FruitBlock;
import com.ferreusveritas.dynamictrees.block.PodBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.WaterSoilProperties;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        WailaBranchHandler branchHandler = new WailaBranchHandler();
        registration.registerBlockComponent(branchHandler, BranchBlock.class);
        registration.registerBlockComponent(branchHandler, TrunkShellBlock.class);

        registration.registerBlockComponent(new WailaFruitHandler(), FruitBlock.class);
        registration.registerBlockComponent(new WailaPodHandler(), PodBlock.class);
        registration.registerBlockComponent(new WailaRootyHandler(), RootyBlock.class);
        registration.registerBlockComponent(new WailaRootyWaterHandler(), WaterSoilProperties.RootyWaterBlock.class);
    }

}
