package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.EnumChance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDiscProviderUniversal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.RandomXOR;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase.BiomeEntry;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class TreeGenerator {

	protected static TreeGenerator INSTANCE;

	protected final PoissonDiscProviderUniversal circleProvider;
	protected final RandomXOR random = new RandomXOR();

	public static void setup() {
		new TreeGenerator();
	}

	public static TreeGenerator getTreeGenerator() {
		return INSTANCE;
	}

	public TreeGenerator() {
		INSTANCE = this; // Set this here in case the lines in the contructor lead to calls that use getTreeGenerator
		this.circleProvider = new PoissonDiscProviderUniversal();
	}

	/**
	 * This is for world gen debugging.
	 * The colors signify the different tree spawn failure modes.
	 */
	public enum EnumGeneratorResult {
		GENERATED(DyeColor.WHITE),
		NO_TREE(DyeColor.BLACK),
		UNHANDLED_BIOME(DyeColor.YELLOW),
		FAIL_SOIL(DyeColor.BROWN),
		FAIL_CHANCE(DyeColor.BLUE),
		FAIL_GENERATION(DyeColor.RED),
		NO_GROUND(DyeColor.PURPLE);

		private final DyeColor color;

		EnumGeneratorResult(DyeColor color) {
			this.color = color;
		}

		public DyeColor getColor() {
			return this.color;
		}

	}

	public PoissonDiscProviderUniversal getCircleProvider() {
		return circleProvider;
	}

	public void makeConcreteCircle(IWorld world, PoissonDisc circle, int h, EnumGeneratorResult resultType, SafeChunkBounds safeBounds) {
		makeConcreteCircle(world, circle, h, resultType, safeBounds, 0);
	}

	private BlockState getConcreteByColor(DyeColor color){
		return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(color + "_concrete"))).getDefaultState();
	}

	public void makeConcreteCircle(IWorld world, PoissonDisc circle, int h, EnumGeneratorResult resultType, SafeChunkBounds safeBounds, int flags) {
		for(int ix = -circle.radius; ix <= circle.radius; ix++) {
			for(int iz = -circle.radius; iz <= circle.radius; iz++) {
				if(circle.isEdge(circle.x + ix, circle.z + iz)) {
					safeBounds.setBlockState(world, new BlockPos(circle.x + ix, h, circle.z + iz), getConcreteByColor(DyeColor.byId((circle.x ^ circle.z) & 0xF)), flags, true);
				}
			}
		}

		if(resultType != EnumGeneratorResult.GENERATED) {
			BlockPos pos = new BlockPos(circle.x, h, circle.z);
			DyeColor color = resultType.getColor();
			safeBounds.setBlockState(world, pos, getConcreteByColor(color), true);
			safeBounds.setBlockState(world, pos.up(), getConcreteByColor(color), true);
		}
	}

	public EnumGeneratorResult makeTree(ISeedReader world, BiomeDatabase biomeDataBase, PoissonDisc circle, IGroundFinder groundFinder, SafeChunkBounds safeBounds) {

		circle.add(8, 8);//Move the circle into the "stage"

		BlockPos pos = new BlockPos(circle.x, 0, circle.z);

		Biome biome = world.getBiome(pos);
		BiomeEntry biomeEntry = biomeDataBase.getEntry(biome);

		pos = groundFinder.findGround(biomeEntry, world, pos);

		if(pos == BlockPos.ZERO) {
			return EnumGeneratorResult.NO_GROUND;
		}

		random.setXOR(pos);

		BlockState dirtState = world.getBlockState(pos);

		EnumGeneratorResult result = EnumGeneratorResult.GENERATED;

		BiomePropertySelectors.ISpeciesSelector speciesSelector = biomeEntry.getSpeciesSelector();
		SpeciesSelection speciesSelection = speciesSelector.getSpecies(pos, dirtState, random);
		if(speciesSelection.isHandled()) {
			Species species = speciesSelection.getSpecies();
			if(species.isValid()) {
				if(species.isAcceptableSoilForWorldgen(world, pos, dirtState)) {
					if(biomeEntry.getChanceSelector().getChance(random, species, circle.radius) == EnumChance.OK) {
						if(!species.generate(world.getWorld(), world, pos, biome, random, circle.radius, safeBounds)) {
							result = EnumGeneratorResult.FAIL_GENERATION;
						}
					} else {
						result = EnumGeneratorResult.FAIL_CHANCE;
					}
				} else {
					result = EnumGeneratorResult.FAIL_SOIL;
				}
			} else {
				result = EnumGeneratorResult.NO_TREE;
			}
		} else {
			result = EnumGeneratorResult.UNHANDLED_BIOME;
		}

		//Display wool circles for testing the circle growing algorithm
		if(DTConfigs.worldGenDebug.get()) {
			makeConcreteCircle(world, circle, pos.getY(), result, safeBounds);
		}

		circle.add(-8, -8);//Move the circle back to normal coords

		return result;
	}

}
