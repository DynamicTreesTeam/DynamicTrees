package com.ferreusveritas.dynamictrees.worldgen;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.PoissonDisc;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.BiomeEntry;

import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class TreeGenerator {
	
	protected static TreeGenerator INSTANCE;
	
	protected final BiomeDataBase defaultBiomeDataBase;
	public static final BiomeDataBase DIMENSIONBLACKLISTED = new BiomeDataBase();
	protected final PoissonDiscProviderUniversal circleProvider;
	protected final RandomXOR random = new RandomXOR();
	protected final Map<Integer, BiomeDataBase> dimensionMap = new HashMap<>();
	
	public static void preInit() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			new TreeGenerator();
		}
	}
	
	public TreeGenerator() {
		INSTANCE = this;//Set this here in case the lines in the contructor lead to calls that use getTreeGenerator
		defaultBiomeDataBase = new BiomeDataBase();
		circleProvider = new PoissonDiscProviderUniversal(this);
	}
	
	public static TreeGenerator getTreeGenerator() {
		return INSTANCE;
	}
	
	public BiomeDataBase getBiomeDataBase(int dimensionId) {
		return dimensionMap.getOrDefault(dimensionId, getDefaultBiomeDataBase());
	}
	
	public BiomeDataBase getBiomeDataBase(World world) {
		return getBiomeDataBase(world.provider.getDimension());
	}
	
	public BiomeDataBase getDefaultBiomeDataBase() {
		return defaultBiomeDataBase;
	}
	
	public void linkDimensionToDataBase(int dimensionId, BiomeDataBase dBase) {
		dimensionMap.put(dimensionId, dBase);
	}
	
	public void BlackListDimension(int dimensionId) {
		dimensionMap.put(dimensionId, DIMENSIONBLACKLISTED);
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
	
	public PoissonDiscProviderUniversal getCircleProvider() {
		return circleProvider;
	}
	
	public void makeWoolCircle(World world, PoissonDisc circle, int h, EnumGeneratorResult resultType) {
		makeWoolCircle(world, circle, h, resultType, 0);
	}
	
	public void makeWoolCircle(World world, PoissonDisc circle, int h, EnumGeneratorResult resultType, int flags) {
		
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
	
	public EnumGeneratorResult makeTree(World world, BiomeDataBase biomeDataBase, PoissonDisc circle, IGroundFinder groundFinder, SafeChunkBounds safeBounds) {
		
		circle.add(8, 8);//Move the circle into the "stage"
		
		BlockPos pos = new BlockPos(circle.x, 0, circle.z);
		
		Biome biome = world.getBiome(pos);
		BiomeEntry biomeEntry = biomeDataBase.getEntry(biome);
		
		pos = groundFinder.findGround(biomeEntry, world, pos);
		
		if(pos == BlockPos.ORIGIN) {
			return EnumGeneratorResult.NOGROUND;
		}
		
		random.setXOR(pos);
		
		IBlockState dirtState = world.getBlockState(pos);
		
		EnumGeneratorResult result = EnumGeneratorResult.GENERATED;
		
		SpeciesSelection speciesSelection = biomeEntry.getSpeciesSelector().getSpecies(pos, dirtState, random);
		if(speciesSelection.isHandled()) {
			Species species = speciesSelection.getSpecies();
			if(species != null) {
				if(species.isAcceptableSoilForWorldgen(world, pos, dirtState)) {
					if(biomeEntry.getChanceSelector().getChance(random, species, circle.radius) == EnumChance.OK) {
						if(species.generate(world, pos, biome, random, circle.radius, safeBounds)) {
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
