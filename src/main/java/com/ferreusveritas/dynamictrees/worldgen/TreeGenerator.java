package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDiscProviderUniversal;

import java.util.Map;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.BiomeEntry;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.Dimension;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public class TreeGenerator {

	protected static TreeGenerator INSTANCE;

	protected final BiomeDataBase defaultBiomeDataBase;
	public static final BiomeDataBase DIMENSIONBLACKLISTED = new BiomeDataBase();
	protected final PoissonDiscProviderUniversal circleProvider;
	protected final RandomXOR random = new RandomXOR();
	protected final Map<Dimension, BiomeDataBase> dimensionMap = new HashMap<>();

	public static void setup() {
		if(WorldGenRegistry.isWorldGenEnabled()) {
			new TreeGenerator();
		}
	}

	public TreeGenerator() {
		INSTANCE = this;//Set this here in case the lines in the contructor lead to calls that use getTreeGenerator
		defaultBiomeDataBase = new BiomeDataBase();
		circleProvider = new PoissonDiscProviderUniversal();
	}

	public static TreeGenerator getTreeGenerator() {
		return INSTANCE;
	}

	public BiomeDataBase getBiomeDataBase(Dimension dimension) {
		return dimensionMap.getOrDefault(dimension, getDefaultBiomeDataBase());
	}

	public BiomeDataBase getBiomeDataBase(World world) {
		return getBiomeDataBase(world.getDimension());
	}

	public BiomeDataBase getDefaultBiomeDataBase() {
		return defaultBiomeDataBase;
	}

	public void linkDimensionToDataBase(Dimension dimension, BiomeDataBase dBase) {
		dimensionMap.put(dimension, dBase);
	}

	public void BlackListDimension(Dimension dimension) {
		System.out.println("DynamicTrees Applying BlackListed Dimension: " + dimension.toString());
		dimensionMap.put(dimension, DIMENSIONBLACKLISTED);
	}

	public void clearAllBiomeDataBases() {
		dimensionMap.clear();
		defaultBiomeDataBase.clear();
	}

	public boolean validateBiomeDataBases() {
		return defaultBiomeDataBase.isValid() && dimensionMap.values().stream().allMatch(db -> db.isValid());
	}

	/**
	 * This is for world debugging.
	 * The colors signify the different tree spawn failure modes.
	 *
	 */
	public enum EnumGeneratorResult {
		GENERATED(DyeColor.WHITE),
		NOTREE(DyeColor.BLACK),
		UNHANDLEDBIOME(DyeColor.YELLOW),
		FAILSOIL(DyeColor.BROWN),
		FAILCHANCE(DyeColor.BLUE),
		FAILGENERATION(DyeColor.RED),
		NOGROUND(DyeColor.PURPLE);

		private final DyeColor color;

		private EnumGeneratorResult(DyeColor color) {
			this.color = color;
		}

		public DyeColor getColor() {
			return this.color;
		}

	}

	public PoissonDiscProviderUniversal getCircleProvider() {
		return circleProvider;
	}

	public void makeWoolCircle(World world, PoissonDisc circle, int h, EnumGeneratorResult resultType, SafeChunkBounds safeBounds) {
		makeWoolCircle(world, circle, h, resultType, safeBounds, 0);
	}

	private BlockState getWoolByColor(DyeColor color){
		return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(color+"_wool")).getDefaultState();
	}

	public void makeWoolCircle(World world, PoissonDisc circle, int h, EnumGeneratorResult resultType, SafeChunkBounds safeBounds, int flags) {

		for(int ix = -circle.radius; ix <= circle.radius; ix++) {
			for(int iz = -circle.radius; iz <= circle.radius; iz++) {
				if(circle.isEdge(circle.x + ix, circle.z + iz)) {
					safeBounds.setBlockState(world, new BlockPos(circle.x + ix, h, circle.z + iz), getWoolByColor(DyeColor.byId((circle.x ^ circle.z) & 0xF)), flags, true);
				}
			}
		}

		if(resultType != EnumGeneratorResult.GENERATED) {
			BlockPos pos = new BlockPos(circle.x, h, circle.z);
			DyeColor color = resultType.getColor();
			safeBounds.setBlockState(world, pos, getWoolByColor(color), true);
			safeBounds.setBlockState(world, pos.up(), getWoolByColor(color), true);
		}
	}

	public EnumGeneratorResult makeTree(World world, BiomeDataBase biomeDataBase, PoissonDisc circle, IGroundFinder groundFinder, SafeChunkBounds safeBounds) {

		circle.add(8, 8);//Move the circle into the "stage"

		BlockPos pos = new BlockPos(circle.x, 0, circle.z);

		Biome biome = world.getBiome(pos);
		BiomeEntry biomeEntry = biomeDataBase.getEntry(biome);

		pos = groundFinder.findGround(biomeEntry, world, pos);

		if(pos == BlockPos.ZERO) {
			return EnumGeneratorResult.NOGROUND;
		}

		random.setXOR(pos);

		BlockState dirtState = world.getBlockState(pos);

		EnumGeneratorResult result = EnumGeneratorResult.GENERATED;

		SpeciesSelection speciesSelection = biomeEntry.getSpeciesSelector().getSpecies(pos, dirtState, random);
		if(speciesSelection.isHandled()) {
			Species species = speciesSelection.getSpecies();
			if(species.isValid()) {
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
		if(DTConfigs.worldGenDebug.get()) {
			makeWoolCircle(world, circle, pos.getY(), result, safeBounds);
		}

		circle.add(-8, -8);//Move the circle back to normal coords

		return result;
	}

}
