package com.ferreusveritas.dynamictrees.models.bakedmodels;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.util.Connections;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class ModelConnections extends Connections implements IModelData {
	
	//ModelProperty<Integer>[] radii = new ModelProperty<Integer>[6];
	
	public ModelConnections() { }
	
	public ModelConnections(Connections connections) {
		this.setAllRadii(connections.getAllRadii());
	}
	
	public ModelConnections(int[] radii) {
		super(radii);
	}
	
	public ModelConnections setAllRadii (int[] radii){
		return (ModelConnections) super.setAllRadii(radii);
	}
	
	@Override
	public boolean hasProperty(ModelProperty<?> prop) {
		return false;
	}
	
	@Nullable
	@Override
	public <T> T getData(ModelProperty<T> prop) {
		return null;
	}
	
	@Nullable
	@Override
	public <T> T setData(ModelProperty<T> prop, T data) {
		return null;
	}
	
}
