package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class Tooltips {

    public static void applySeasonalTooltips(List<Component> tipList, int flags, ClimateZoneType climate) {
        if (flags == -1) return;
        if (flags == 0) {
            tipList.add(Component.translatable("desc.dynamictrees.seasonal.infertile", Component.translatable(climate.unlocalizedName)).withStyle(ChatFormatting.RED));
        }
        else {
            tipList.add(Component.translatable("desc.dynamictrees.seasonal.fertile_seasons").append(" (").append(Component.translatable(climate.unlocalizedName)).append("):"));

            if ((flags & 15) == 15) {
                tipList.add(Component.literal(" ").append(Component.translatable("desc.dynamictrees.seasonal.year_round").withStyle(ChatFormatting.LIGHT_PURPLE)));
            } else {
                if ((flags & 1) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.dynamictrees.seasonal.spring").withStyle(ChatFormatting.GREEN)));
                }
                if ((flags & 2) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.dynamictrees.seasonal.summer").withStyle(ChatFormatting.YELLOW)));
                }
                if ((flags & 4) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.dynamictrees.seasonal.autumn").withStyle(ChatFormatting.GOLD)));
                }
                if ((flags & 8) != 0) {
                    tipList.add(Component.literal(" ").append(Component.translatable("desc.dynamictrees.seasonal.winter").withStyle(ChatFormatting.AQUA)));
                }
            }
        }
    }

}
