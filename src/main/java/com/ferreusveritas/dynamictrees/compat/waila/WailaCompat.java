package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.PodBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.WaterSoilProperties;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        WailaBranchHandler branchHandler = new WailaBranchHandler();
        WailaRootyHandler rootyHandler = new WailaRootyHandler();

        registrar.registerComponentProvider(branchHandler, TooltipPosition.BODY, BranchBlock.class);
        registrar.registerComponentProvider(branchHandler, TooltipPosition.BODY, TrunkShellBlock.class);
        registrar.registerComponentProvider(rootyHandler, TooltipPosition.BODY, RootyBlock.class);
        registrar.registerComponentProvider(new WailaPodHandler(), TooltipPosition.BODY, PodBlock.class);
        registrar.registerComponentProvider(new WailaFruitHandler(), TooltipPosition.BODY, FruitBlock.class);
        registrar.registerComponentProvider(new WailaRootyWaterHandler(), TooltipPosition.HEAD, WaterSoilProperties.RootyWaterBlock.class);
    }

}
