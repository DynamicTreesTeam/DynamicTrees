package com.ferreusveritas.growingtrees.trees;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.blocks.GrowSignal;
import com.ferreusveritas.growingtrees.special.BottomListenerPodzol;
import com.ferreusveritas.growingtrees.special.BottomListenerVine;

import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.ForgeDirection;

public class TreeJungle extends GrowingTree {

	public TreeJungle(int seq) {
		super("jungle", seq);
		
		//Jungle Trees are tall, wildly growing, fast growing trees with low branches to provide inconvenient obstruction and climbing
		setBasicGrowingParameters(0.15f, 16.0f, 3, 2, 1.0f);

		retries = 2;//Very fast growing
		soilLongevity = 10;//Lasts a bit longer than average(8)
		
		setPrimitiveLeaves(Blocks.leaves, 3);//Vanilla Jungle Leaves
		setPrimitiveLog(Blocks.log, 3);//Vanilla Jungle Log
		setPrimitiveSapling(Blocks.sapling, 3);

		envFactor(Type.COLD, 0.15f);
		envFactor(Type.DRY,  0.20f);
		envFactor(Type.HOT, 1.1f);
		envFactor(Type.WET, 1.1f);
		
		registerBottomSpecials(new BottomListenerPodzol(), new BottomListenerVine());
		
	}

	@Override
	protected int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]) {
		//Jungle Trees grow a maximum of 16 meters tall
		
		//Amplify cardinal directions to encourage spread
		float spreadPush = 1.0f + signal.dy / getEnergy(world, x, y, z) * 6;// 1(bottom) to 6(top)
		
		for(ForgeDirection dir: GrowingTrees.cardinalDirs){
			probMap[dir.ordinal()] *= spreadPush;
		}
		
		//Contort up direction based on distance from ground
		int up = ForgeDirection.UP.ordinal();
		int down = ForgeDirection.DOWN.ordinal();
		
		probMap[up] *= MathHelper.clamp_int((int)(biomeSuitability(world, x, y, z) * 4) - (signal.dy / 4), 1, 4);//4(bottom) - 1(top)

		//Pull down the farther cardinally we are from the base
		int dist = (int)Math.sqrt(signal.dx * signal.dx + signal.dz * signal.dz);
		probMap[down] *= MathHelper.clamp_int(dist / 4, 1, 4);
		
		return probMap;
	}
	
	//Jungle trees grow taller in suitable biomes
	@Override
	public float getEnergy(World world, int x, int y, int z) {
        return super.getEnergy(world, x, y, z) * biomeSuitability(world, x, y, z);
	}
	
	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE);
	};
	
}
