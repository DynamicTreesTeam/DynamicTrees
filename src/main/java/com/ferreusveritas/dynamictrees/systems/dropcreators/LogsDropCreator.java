package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Species.LogsAndSticks;
import net.minecraft.util.ResourceLocation;

public class LogsDropCreator extends DropCreator {

	public LogsDropCreator() {
		super(new ResourceLocation(DynamicTrees.MOD_ID, "logs"));
	}

	@Override
	protected void registerProperties() { }

	@Override
	public void appendLogDrops(ConfiguredDropCreator<DropCreator> configuration, LogDropContext context) {
		final Species species = context.species();
		final LogsAndSticks las = species.getLogsAndSticks(context.volume());

		int numLogs = las.logs.size();
		if (numLogs > 0) {
			context.drops().addAll(las.logs);
		}
		int numSticks = las.sticks;
		if (numSticks > 0) {
			context.drops().add(species.getFamily().getStick(numSticks));
		}
	}

}
