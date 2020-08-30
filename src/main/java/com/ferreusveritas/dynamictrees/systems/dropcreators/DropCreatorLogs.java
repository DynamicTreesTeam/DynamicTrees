package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Species.LogsAndSticks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class DropCreatorLogs extends DropCreator {

	public DropCreatorLogs() {
		super(new ResourceLocation(DynamicTrees.MODID, "logs"));
	}

	@Override
	public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, float volume) {
		LogsAndSticks las = species.getLogsAndSticks(volume);
		
		int numLogs = las.logs;
		while(numLogs > 0) {
			dropList.add(species.getFamily().getPrimitiveLogs(Math.min(numLogs, 64)));
			numLogs -= 64;
		}
		int numSticks = las.sticks;
		if(numSticks > 0) {
			dropList.add(species.getFamily().getStick(numSticks));
		}
		return dropList;
	}

}
