package com.ferreusveritas.dynamictrees.models;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ModelLoaderBranch implements ICustomModelLoader {
	public final String BRANCH_MODEL_RESOURCE_NAME = "dynamictrees:block/smartmodel/branch";
	public final String code = "#dynamictree";
	
	// return true if our Model Loader accepts this ModelResourceLocation
	@Override
	public boolean accepts(ResourceLocation resourceLocation) {
		return resourceLocation.getResourcePath().endsWith(code);//Hacky but fast
	}

	// When called for our BlockBranch's ModelResourceLocation, return our BranchModel.
	@Override
	public IModel loadModel(ResourceLocation resourceLocation) {
		ModelBlock modelBlock = getBranchBlockModel(resourceLocation);//We need this to model textures.
		return (modelBlock != null) ? new BranchModel(modelBlock) : ModelLoaderRegistry.getMissingModel();
	}

	protected ModelBlock getBranchBlockModel(ResourceLocation virtualLocation) {
		
		if(!accepts(virtualLocation)) {
			return null;
		}
		
		String path = virtualLocation.getResourcePath();//Extract the path portion of the ResourceLocation
		path = path.substring(0, path.length() - code.length());//Remove the ending code from the location
		ResourceLocation location = new ResourceLocation(virtualLocation.getResourceDomain(), path);//Recreate the resource location without the code
		
		ModelBlock modelBlock = null;
		Reader reader = null;
		IResource iresource = null;
		
		try {
			iresource = this.resourceManager.getResource(this.getModelLocation(location));
			reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);
			modelBlock = ModelBlock.deserialize(reader);
			modelBlock.name = location.toString();
			
			ModelBlock rootParent = modelBlock;
			
			//Climb the hierarchy to discover the name of the root parent model
			while(rootParent.parent != null) {
				rootParent = rootParent.parent;
			}
			
			//If the name of the parent node is our model then we're good to go.
			if(rootParent.getParentLocation() != null && rootParent.getParentLocation().toString().equals(BRANCH_MODEL_RESOURCE_NAME) ) {
				return modelBlock;
			}
			
			return null;
			
		} catch (IOException e) {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(iresource);
		}
		
		return null;
	}
	
	protected ResourceLocation getModelLocation(ResourceLocation location) {
		return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".json");
	}
	
	// don't need it for this example; you might.  We have to implement it anyway.
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	private IResourceManager resourceManager;
	}