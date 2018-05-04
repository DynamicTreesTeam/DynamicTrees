package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.Circle;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.BiomeEntry;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class TreeGenerator {
	
	private static TreeGenerator INSTANCE;
	
	public BiomeDataBase biomeDataBase;
	public BiomeRadiusCoordinator radiusCoordinator; //Finds radius for coordinates
	public JoCodeStore codeStore;
	protected ChunkCircleManager circleMan;
	protected RandomXOR random;
	
	public static TreeGenerator getTreeGenerator() {
		return INSTANCE;
	}
	
	public static void preInit() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			INSTANCE = new TreeGenerator();
		}
	}
	
	/**
	 * This is for world debugging.
	 * The colors signify the different tree spawn failure modes.
	 *
	 */
	public enum EnumGeneratorResult {
		GENERATED(EnumDyeColor.WHITE),
		NOTREE(EnumDyeColor.BLACK),
		UNHANDLEDBIOME(EnumDyeColor.YELLOW),
		FAILSOIL(EnumDyeColor.BROWN),
		FAILCHANCE(EnumDyeColor.BLUE),
		FAILGENERATION(EnumDyeColor.RED),
		NOGROUND(EnumDyeColor.PURPLE);
		
		private final EnumDyeColor color;
		
		private EnumGeneratorResult(EnumDyeColor color) {
			this.color = color;
		}
		
		public EnumDyeColor getColor() {
			return this.color;
		}
	
	}
	
	public TreeGenerator() {
		biomeDataBase = new BiomeDataBase();
		radiusCoordinator = new BiomeRadiusCoordinator(biomeDataBase);
		circleMan = new ChunkCircleManager(radiusCoordinator);
		random = new RandomXOR();
	}
	
	public void onWorldUnload() {
		circleMan = new ChunkCircleManager(radiusCoordinator);//Clears the cached circles
	}
	
	public ChunkCircleManager getChunkCircleManager() {
		return circleMan;
	}
		
	public void generate(World world, Biome biome, ChunkPos chunkPos) {
		//We use this custom random number generator because despite what everyone says the Java Random class is not thread safe.
		random.setXOR(new BlockPos(chunkPos.x, 0, chunkPos.z));
		
		circleMan.getCircles(world, random, chunkPos.x, chunkPos.z).forEach(c -> makeTree(world, c));
	}
	
	public void makeWoolCircle(World world, Circle circle, int h, EnumGeneratorResult resultType) {
		makeWoolCircle(world, circle, h, resultType, 0);
	}
	
	public void makeWoolCircle(World world, Circle circle, int h, EnumGeneratorResult resultType, int flags) {
		
		for(int ix = -circle.radius; ix <= circle.radius; ix++) {
			for(int iz = -circle.radius; iz <= circle.radius; iz++) {
				if(circle.isEdge(circle.x + ix, circle.z + iz)) {
					world.setBlockState(new BlockPos(circle.x + ix, h, circle.z + iz), Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.byMetadata((circle.x ^ circle.z) & 0xF)), flags);
				}
			}
		}
		
		if(resultType != EnumGeneratorResult.GENERATED) {
			BlockPos pos = new BlockPos(circle.x, h, circle.z);
			EnumDyeColor color = resultType.getColor();
			world.setBlockState(pos, Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, color));
			world.setBlockState(pos.up(), Blocks.CARPET.getDefaultState().withProperty(BlockColored.COLOR, color));
		}
	}
	
	private EnumGeneratorResult makeTree(World world, Circle circle) {
		
		circle.add(8, 8);//Move the circle into the "stage"
		
		BlockPos pos = world.getHeight(new BlockPos(circle.x, 0, circle.z)).down();
		while(world.isAirBlock(pos) || TreeHelper.isTreePart(world, pos)) {//Skip down past the bits of generated tree and air
			pos = pos.down();
			if(pos.getY() < 0) {
				return EnumGeneratorResult.NOGROUND;
			}
		}
		
		IBlockState blockState = world.getBlockState(pos);
		
		EnumGeneratorResult result = EnumGeneratorResult.GENERATED;
		
		Biome biome = world.getBiome(pos);
		BiomeEntry biomeEntry = biomeDataBase.getEntry(biome);
		
		SpeciesSelection speciesSelection = biomeEntry.getSpeciesSelector().getSpecies(pos, blockState, random);
		if(speciesSelection.isHandled()) {
			Species species = speciesSelection.getSpecies();
			if(species != null) {
				if(species.isAcceptableSoilForWorldgen(world, pos, blockState)) {
					if(biomeEntry.getChanceSelector().getChance(random, species, circle.radius) == EnumChance.OK) {
						if(species.generate(world, pos, biome, random, circle.radius)) {
							result = EnumGeneratorResult.GENERATED;
						} else {
							result = EnumGeneratorResult.FAILGENERATION;
						}
					} else {
						result = EnumGeneratorResult.FAILCHANCE;
					}
				} else {
					result = EnumGeneratorResult.FAILSOIL;
				}
			} else {
				result = EnumGeneratorResult.NOTREE;
			}
		} else {
			result = EnumGeneratorResult.UNHANDLEDBIOME;
		}
		
		//Display wool circles for testing the circle growing algorithm
		if(ModConfigs.worldGenDebug) {
			makeWoolCircle(world, circle, pos.getY(), result);
		}
		
		circle.add(-8, -8);//Move the circle back to normal coords
		
		return result;
	}
	
}
