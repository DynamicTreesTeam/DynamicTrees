package com.dtteam.dynamictrees.compat.waila;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.fruit.FruitBlock;
import com.dtteam.dynamictrees.block.pod.PodBlock;
import com.dtteam.dynamictrees.block.soil.RootyBlock;
import com.dtteam.dynamictrees.block.soil.WaterSoilProperties;
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
