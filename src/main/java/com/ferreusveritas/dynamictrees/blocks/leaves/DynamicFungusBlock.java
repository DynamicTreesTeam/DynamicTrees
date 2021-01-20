package com.ferreusveritas.dynamictrees.blocks.leaves;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class DynamicFungusBlock extends DynamicLeavesBlock {

    public DynamicFungusBlock() {
        super(Properties.create(Material.WOOD).tickRandomly().hardnessAndResistance(0.2F).sound(SoundType.WOOD).harvestTool(ToolType.AXE));
    }

}
