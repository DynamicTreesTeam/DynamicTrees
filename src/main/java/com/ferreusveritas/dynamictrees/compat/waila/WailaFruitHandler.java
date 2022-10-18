package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class WailaFruitHandler implements IComponentProvider {

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof FruitBlock) {
            FruitBlock fruitBlock = (FruitBlock) accessor.getBlock();
            float ageAsPercentage = fruitBlock.getAgeAsPercentage(accessor.getBlockState());
            tooltip.add(new TranslationTextComponent(
                    "tooltip.waila.crop_growth",
                    ageAsPercentage < 100F ? String.format("%.0f%%", ageAsPercentage) :
                            new TranslationTextComponent("tooltip.waila.crop_mature").withStyle(TextFormatting.GREEN)
            ));
        }
    }

}
