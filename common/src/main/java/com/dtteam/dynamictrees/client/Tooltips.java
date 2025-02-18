package com.dtteam.dynamictrees.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class Tooltips {

    public static void applySeasonalTooltips(List<Component> tipList, int flags) {
        if (flags != 0) {
            tipList.add(Component.translatable("desc.dynamictrees.seasonal.fertile_seasons").append(":"));

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
