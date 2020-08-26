package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;


/**
 *
 * @author ferreusveritas
 *
 */
@OnlyIn(Dist.CLIENT)
public class BakedModelSapling implements IDynamicBakedModel {

	private static IBakedModel[] modelMap;
	private static IBakedModel errorSaplingModel;
	private static TextureAtlasSprite particleTexture;

	public static IBakedModel getModelForSapling(Species species) {
		if (species == null || !species.isValid()){
			return errorSaplingModel;
		}
		int modelId = species.saplingModelId;
		IBakedModel bakedModel = modelMap[modelId];
		return bakedModel != null ? bakedModel : errorSaplingModel;
	}



	public BakedModelSapling() {

		try {
			errorSaplingModel = Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(DynamicTrees.MODID, "block/saplings/error"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		particleTexture = Minecraft.getInstance().getTextureMap().getAtlasSprite("block/dirt");

		modelMap = new IBakedModel[Species.REGISTRY.getEntries().size()];

		int modelId = 0;

		for(Entry<ResourceLocation, Species> entry : Species.REGISTRY.getEntries()) {
			Species species = entry.getValue();
			ResourceLocation resLoc = species.getSaplingName();
			species.saplingModelId = modelId;
			if(species != Species.NULLSPECIES) {
				try {
					modelMap[modelId] = Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(resLoc.getNamespace(), "block/saplings/" + resLoc.getPath()));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			modelId++;
		}

	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

		if (side != null){
			return Collections.emptyList();
		}

		List<BakedQuad> quads = new ArrayList<>(12);
//		Species species = extraData.getData(TileEntitySpecies.SPECIES);
//
//		quads.addAll(getModelForSapling(species).getQuads(DTRegistries.blockDynamicSapling.getDefaultState(), side, rand, extraData));

		return quads;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return particleTexture;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.EMPTY;
	}

}
