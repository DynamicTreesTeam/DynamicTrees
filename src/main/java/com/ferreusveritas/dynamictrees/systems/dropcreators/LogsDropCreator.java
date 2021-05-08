package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Species.LogsAndSticks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class LogsDropCreator extends DropCreator {

	public LogsDropCreator() {
		super(new ResourceLocation(DynamicTrees.MOD_ID, "logs"));
	}

	@Override
	protected void registerProperties() { }

	@Override
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> drops, NetVolumeNode.Volume volume) {
		LogsAndSticks las = species.getLogsAndSticks(volume);

		int numLogs = las.logs.size();
		if(numLogs > 0) {
			drops.addAll(las.logs);
		}
		int numSticks = las.sticks;
		if(numSticks > 0) {
			drops.add(species.getFamily().getStick(numSticks));
		}
		return drops;
	}

}
