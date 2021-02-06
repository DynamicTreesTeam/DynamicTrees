package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeNetVolume;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Species.LogsAndSticks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class LogDropCreator extends DropCreator {

	public LogDropCreator() {
		super(new ResourceLocation(DynamicTrees.MOD_ID, "logs"));
	}

	@Override
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, NodeNetVolume.Volume volume) {
		LogsAndSticks las = species.getLogsAndSticks(volume);

		int numLogs = las.logs.size();
		if(numLogs > 0) {
			dropList.addAll(las.logs);
		}
		int numSticks = las.sticks;
		if(numSticks > 0) {
			dropList.add(species.getFamily().getStick(numSticks));
		}
		return dropList;
	}

}
