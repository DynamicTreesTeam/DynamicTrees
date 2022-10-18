package com.ferreusveritas.dynamictrees.compat.waila;

import com.ferreusveritas.dynamictrees.blocks.PodBlock;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class WailaPodHandler implements IComponentProvider {

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if (accessor.getBlock() instanceof PodBlock) {
            PodBlock podBlock = (PodBlock) accessor.getBlock();
            float ageAsPercentage = podBlock.getAgeAsPercentage(accessor.getBlockState());
            tooltip.add(new TranslationTextComponent(
                    "tooltip.waila.crop_growth",
                    ageAsPercentage < 100F ? String.format("%.0f%%", ageAsPercentage) :
                            new TranslationTextComponent("tooltip.waila.crop_mature").withStyle(TextFormatting.GREEN)
            ));
        }
    }

}
