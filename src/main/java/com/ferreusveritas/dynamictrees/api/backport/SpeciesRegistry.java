package com.ferreusveritas.dynamictrees.api.backport;

import java.util.HashMap;
import java.util.Iterator;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.util.ResourceLocation;

public class SpeciesRegistry implements Iterable<Species> {

	private HashMap<ResourceLocation, Species> hashmap;
	
	public SpeciesRegistry() {
		hashmap = new HashMap<ResourceLocation, Species>();
	}
	
	public Species register(Species species) {
		hashmap.put(species.getRegistryName(), species);
		return species;
	}

	public void registerAll(Species ... values) {
		for(Species s : values) {
			register(s);
		}
	}
	
	public Species find(ResourceLocation name) {
		return hashmap.get(name);
	}
	
	public boolean containsKey(ResourceLocation name) {
		return hashmap.containsKey(name);
	}
	
	public Species getValue(ResourceLocation name) {
		return hashmap.get(name);
	}
	
	@Override
	public Iterator<Species> iterator() {
		return hashmap.values().iterator();
	}
}
