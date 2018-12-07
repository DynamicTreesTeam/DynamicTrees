package com.ferreusveritas.dynamictrees.models;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public class ModelResourceLocationWithState extends ModelResourceLocation {

	public String payload;
	
	public ModelResourceLocationWithState(String location) {
		super(location);
		payload = "testme!";
	}
	
}
