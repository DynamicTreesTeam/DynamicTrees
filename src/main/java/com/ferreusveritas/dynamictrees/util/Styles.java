package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

/**
 * @author Harley O'Connor
 */
public final class Styles {
	
	public static final Style ERROR = new Style() {
		@Override
		public TextFormatting getColor() {
			return TextFormatting.RED;
		}
	};
	
}
