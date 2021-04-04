package com.ferreusveritas.dynamictrees.api.treedata;

import net.minecraft.util.ResourceLocation;

import java.util.Map;

public interface IDropCreatorStorage extends IDropCreator {

	boolean addDropCreator(IDropCreator dropCreator);

	IDropCreator findDropCreator(ResourceLocation name);
	
	boolean remDropCreator(ResourceLocation name);
	
	Map<ResourceLocation, IDropCreator> getDropCreators();
	
}
