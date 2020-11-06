package com.ferreusveritas.dynamictrees.models.bakedmodels;

import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class Connections implements IModelData {
	
	private int[] radii;
	//		ModelProperty<Integer>[] radii = new ModelProperty<Integer>[6];
	
	public Connections (){
		radii = new int[] {0,0,0,0,0,0};
	}
	
	public Connections(int[] radii) {
		this.radii = radii;
	}
	
	public void setRadius (Direction dir, int radius){
		radii[dir.getIndex()] = radius;
	}
	
	public int[] getAllRadii (){
		return radii;
	}
	
	public Connections setAllRadii (int[] radii){
		this.radii = radii;
		return this;
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
