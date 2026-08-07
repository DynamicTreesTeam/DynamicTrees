package com.dtteam.dynamictrees.compat.waila;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.fruit.FruitBlock;
import com.dtteam.dynamictrees.block.pod.PodBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.WaterSoilProperties;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Registers DT's Jade tooltip providers.
 *
 * <p>On Fabric, Jade discovers plugins through the {@code "jade"} entrypoint in {@code fabric.mod.json}
 * rather than by scanning for {@link WailaPlugin}, so this class must be listed there as well as
 * annotated. The entrypoint is only ever resolved by Jade itself, so the Jade classes referenced here
 * are never loaded when Jade is absent — which is why Jade can stay a {@code compileOnly} dependency.
 */
@WailaPlugin
public class WailaCompat implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        WailaBranchHandler branchHandler = new WailaBranchHandler();
        registration.registerBlockComponent(branchHandler, BranchBlock.class);
        registration.registerBlockComponent(branchHandler, TrunkShellBlock.class);

        registration.registerBlockComponent(new WailaFruitHandler(), FruitBlock.class);
        registration.registerBlockComponent(new WailaPodHandler(), PodBlock.class);
        registration.registerBlockComponent(new WailaRootyHandler(), SoilBlock.class);
        registration.registerBlockComponent(new WailaRootyWaterHandler(), WaterSoilProperties.SoilWaterBlock.class);
    }

}
