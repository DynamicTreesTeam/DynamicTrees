package com.ferreusveritas.dynamictrees.client;

import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class BlockColorMultipliers {

	private static Map<String, IBlockColor> colorBase = new HashMap<>();

	public static void register(String label, IBlockColor colorMultiplier) {
		colorBase.put(label, colorMultiplier);
	}

	public static void register(ResourceLocation label, IBlockColor colorMultiplier) {
		colorBase.put(label.toString(), colorMultiplier);
	}

	public static IBlockColor find(String label) {
		return colorBase.get(label);
	}

	public static IBlockColor find(ResourceLocation label) {
		return colorBase.get(label.toString());
	}

	public static void cleanUp() {
		colorBase = null;//Once all of the color multipliers have been resolved we no longer need this data structure
	}

}
