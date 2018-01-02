package com.ferreusveritas.dynamictrees.api.treedata;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

public interface IDropCreatorStorage extends IDropCreator {

	public boolean addDropCreator(IDropCreator dropCreator);

	public IDropCreator findDropCreator(ResourceLocation name);
	
	public boolean remDropCreator(ResourceLocation name);
	
	public Map<ResourceLocation, IDropCreator> getDropCreators();
	
}
