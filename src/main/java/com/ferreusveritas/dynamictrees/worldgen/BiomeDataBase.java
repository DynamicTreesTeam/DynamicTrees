package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Collections;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider.IChance;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDensityProvider.IDensity;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeSpeciesSelector.Decision;

public class BiomeDataBase {

	private ArrayList<Entry> data = new ArrayList<Entry>(Collections.nCopies(256, null));
	
	class Entry {
		private IChance chance;
		private IDensity density;
		private Decision species;
		private boolean cancelVanillaTreeGen;
	}
	
}
