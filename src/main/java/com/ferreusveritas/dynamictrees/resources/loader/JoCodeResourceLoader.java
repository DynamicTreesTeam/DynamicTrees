package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.resource.loading.AbstractResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeRegistry;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads {@link JoCode} objects to the {@link JoCodeRegistry}.
 *
 * @author Harley O'Connor
 */
public final class JoCodeResourceLoader extends AbstractResourceLoader<List<String>> {

    private static final Logger LOGGER = LogManager.getLogger();

    public JoCodeResourceLoader() {
        super(new JoCodeResourcePreparer("jo_codes"));
    }

    @Override
    public void applyOnGatherData(ResourceAccessor<List<String>> resourceAccessor, IResourceManager resourceManager) {

    }

    @Override
    public void applyOnLoad(ResourceAccessor<List<String>> resourceAccessor, IResourceManager resourceManager) {

    }

    @Override
    public void applyOnSetup(ResourceAccessor<List<String>> resourceAccessor, IResourceManager resourceManager) {

    }

    @Override
    public void applyOnReload(ResourceAccessor<List<String>> resourceAccessor, IResourceManager resourceManager) {
        JoCodeRegistry.clear();
        resourceAccessor.getAllResources().forEach(resource ->
                this.registerCodes(resource.getLocation(), resource.getResource())
        );
    }

    private void registerCodes(ResourceLocation location, List<String> lines) {
        final Species species = TreeRegistry.findSpecies(location);
        lines.forEach(line -> this.registerCodeForLine(species, line));
        LOGGER.debug("Successfully loaded JoCodes for species \"{}\".", location);
    }

    private void registerCodeForLine(Species species, String line) {
        final String[] radiusAndCode = line.split(":");
        this.registerCode(species, Integer.parseInt(radiusAndCode[0]), radiusAndCode[1]);
    }

    private void registerCode(Species species, int radius, String code) {
        final JoCode joCode = species.getJoCode(code).setCareful(false);

        // Code reserved for collecting WorldGen JoCodes
        //this.collectWorldGenCodes(species, radius, joCode);

        JoCodeRegistry.register(species.getRegistryName(), radius, joCode);
    }

    /**
     * This collects a list of trees and creates 4 variations for the 4 directions and then sorts them alphanumerically.
     * By sorting the rotated JoCodes you can eliminate duplicates who are only different by the direction they are
     * facing.
     *
     * @param radius The radius for the given {@link JoCode}.
     * @param joCode The {@link JoCode} object.
     */
    @SuppressWarnings("unused") // Code reserved for collecting WorldGen JoCodes
    private void collectWorldGenCodes(Species species, int radius, JoCode joCode) {
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        ArrayList<String> arr = new ArrayList<>();

        for (Direction dir : dirs) {
            arr.add(joCode.rotate(dir).toString());
        }

        Collections.sort(arr);
        LOGGER.debug(species + ":" + radius + ":" + arr.get(0));
    }

}
