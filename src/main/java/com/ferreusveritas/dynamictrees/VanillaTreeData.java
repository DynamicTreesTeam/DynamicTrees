package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.backport.BlockState;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class VanillaTreeData {

	public enum EnumType { 
		OAK(0, "oak"),
		SPRUCE(1, "spruce"),
		BIRCH(2, "birch"),
		JUNGLE(3, "jungle"),
		ACACIA(4, "acacia"),
		DARKOAK(5, "darkoak");
		
		private final int meta;
		private final String name;
		
		private EnumType(int meta, String name) {
			this.meta = meta;
			this.name = name;
		}
		
		public int getMetadata() {
			return meta;
		}
		
		public String getName() {
			return name;
		}
		
		public BlockState getLeavesBlockAndMeta() {
			Block vLeaves[] = {Blocks.leaves, Blocks.leaves2};
			return new BlockState(vLeaves[meta >> 2], meta & 3);
		}
		
		public BlockState getLogBlockAndMeta() {
			Block vLeaves[] = {Blocks.log, Blocks.log2};
			return new BlockState(vLeaves[meta >> 2], meta & 3);
		}
	};
	
}
