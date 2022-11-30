package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.block.FruitBlock;
import com.ferreusveritas.dynamictrees.block.PodBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.WaterSoilProperties;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        WailaBranchHandler branchHandler = new WailaBranchHandler();
        registration.registerComponentProvider(branchHandler, TooltipPosition.BODY, BranchBlock.class);
        registration.registerComponentProvider(branchHandler, TooltipPosition.BODY, TrunkShellBlock.class);

        registration.registerComponentProvider(new WailaFruitHandler(), TooltipPosition.BODY, FruitBlock.class);
        registration.registerComponentProvider(new WailaPodHandler(), TooltipPosition.BODY, PodBlock.class);
        registration.registerComponentProvider(new WailaRootyHandler(), TooltipPosition.BODY, RootyBlock.class);
        registration.registerComponentProvider(new WailaRootyWaterHandler(), TooltipPosition.HEAD, WaterSoilProperties.RootyWaterBlock.class);
    }

}
