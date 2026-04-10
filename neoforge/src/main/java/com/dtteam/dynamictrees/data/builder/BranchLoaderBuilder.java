package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.event.handler.ClientModEventHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Harley O'Connor
 */
public final class BranchLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {

    public static final HashMap<Identifier, BiFunction<BlockModelBuilder, ExistingFileHelper, BranchLoaderBuilder>> branchBuilders = new HashMap<>();

    static {
        branchBuilders.put(
                ClientModEventHandler.BRANCH, (parent, fileHelper)->
                        new BranchLoaderBuilder(ClientModEventHandler.BRANCH, parent, fileHelper));
        branchBuilders.put(
                ClientModEventHandler.SURFACE_ROOT, (parent, fileHelper)->
                        new BranchLoaderBuilder(ClientModEventHandler.SURFACE_ROOT, parent, fileHelper));
        branchBuilders.put(
                ClientModEventHandler.ROOTS, (parent, fileHelper)->
                        new BranchLoaderBuilder(ClientModEventHandler.ROOTS, parent, fileHelper));
    }

    private final Map<String, String> textures = new LinkedHashMap<>();

    public BranchLoaderBuilder(Identifier loaderId, BlockModelBuilder parent, ExistingFileHelper fileHelper) {
        super(loaderId, parent, fileHelper, false);
    }

    public BranchLoaderBuilder texture(String key, Identifier location) {
        this.textures.put(key, location.toString());
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        final JsonObject textures = new JsonObject();
        this.textures.forEach((key, location) ->
                textures.add(key, new JsonPrimitive(location)));
        json.add("textures", textures);

        return json;
    }

}
