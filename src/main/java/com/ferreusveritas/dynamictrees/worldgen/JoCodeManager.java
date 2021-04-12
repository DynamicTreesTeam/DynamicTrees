package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.resources.ReloadListener;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Manages {@link JoCode} objects, reading from tree packs. Can be used to call random JoCodes during worldgen.
 * Main instance stored in {@link DTResourceRegistries}.
 *
 * @author Harley O'Connor
 */
public final class JoCodeManager extends ReloadListener<Map<ResourceLocation, List<String>>> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TEXT_EXTENSION = ".txt";
    private static final int TEXT_EXTENSION_LENGTH = TEXT_EXTENSION.length();

    private final Map<ResourceLocation, Map<Integer, List<JoCode>>> joCodes = new HashMap<>();

    public JoCodeManager() {
        super("jo_codes");
    }

    @Nonnull
    @Override
    protected Map<ResourceLocation, List<String>> prepare(final IResourceManager resourceManager) {
        final Map<ResourceLocation, List<String>> joCodeFiles = new HashMap<>();

        for (ResourceLocation resourceLocationIn : resourceManager.listResources(this.folderName,
                (fileName) -> fileName.endsWith(TEXT_EXTENSION))) {
            String resLocStr = resourceLocationIn.getPath();
            ResourceLocation resourceLocation = new ResourceLocation(resourceLocationIn.getNamespace(), resLocStr.substring(this.folderName.length() + 1, resLocStr.length() - TEXT_EXTENSION_LENGTH));

            // Only add the JoCode file if its name matches a species name.
            if (TreeRegistry.findSpecies(resourceLocation) == Species.NULL_SPECIES)
                continue;

            try {
                final IResource resource = resourceManager.getResource(resourceLocationIn);
                InputStream stream = resource.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

                List<String> lines = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    // JoCodes should be at least 1 character (with radius number and colon too), and ignore comments (start with #).
                    if ((line.length() >= 3) && (line.charAt(0) != '#')) {
                        lines.add(line);
                    }
                }

                lines = joCodeFiles.put(resourceLocation, lines);

                if (lines != null) {
                    throw new IllegalStateException("Duplicate data file ignored with ID " + resourceLocation);
                }
            } catch (IOException | IllegalStateException e) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocation, resourceLocationIn);
            }
        }

        return joCodeFiles;
    }

    @Override
    protected void apply(final Map<ResourceLocation, List<String>> preparedObject, final IResourceManager resourceManager, ApplicationType applicationType) {
        this.joCodes.clear();

        preparedObject.forEach((resourceLocation, lines) -> {
            final Species species = TreeRegistry.findSpecies(resourceLocation);

            lines.forEach(line -> {
                final String[] split = line.split(":");
                this.addCode(species, Integer.parseInt(split[0]), split[1]);
            });

            LOGGER.debug("Successfully loaded JoCodes for species '{}'.", resourceLocation);
        });
    }

    private void addCode(Species species, int radius, String code) {
        JoCode joCode = species.getJoCode(code).setCareful(false);

        // Code reserved for collecting WorldGen JoCodes
        //this.collectWorldGenCodes(species, radius, joCode);

        this.joCodes.computeIfAbsent(species.getRegistryName(), s -> new HashMap<>()).computeIfAbsent(radius, r -> new ArrayList<>()).add(joCode);
    }

    /**
     * This collects a list of trees and creates 4 variations for the 4 directions and then
     * sorts them alphanumerically.  By sorting the rotated JoCodes you can eliminate duplicates
     * who are only different by the direction they are facing.
     *
     * @param radius The radius for the given {@link JoCode}.
     * @param joCode The {@link JoCode} object.
     */
    @SuppressWarnings("unused") // Code reserved for collecting WorldGen JoCodes
    private void collectWorldGenCodes(Species species, int radius, JoCode joCode) {
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        ArrayList<String> arr = new ArrayList<>();

        for(Direction dir: dirs) {
            arr.add(joCode.rotate(dir).toString());
        }

        Collections.sort(arr);
        LOGGER.debug(species + ":" + radius + ":" + arr.get(0));
    }

    /**
     * Gets all {@link JoCode} objects for the given species, keyed by the radius.
     *
     * @param species The {@link Species} object to get the codes for.
     * @return A map of integers (radii) and the list of {@link JoCode} objects for that radius,
     *      or an empty map if none were found for the species.
     */
    public Map<Integer, List<JoCode>> getCodes (Species species) {
        return joCodes.getOrDefault(species.getRegistryName(), new HashMap<>());
    }

    /**
     * Gets codes for the given species under the given radius.
     *
     * @param species The {@link Species} object to get the codes for.
     * @param radius The radius of the codes desired.
     * @return The list of {@link JoCode} objects, or an empty list if species doesn't have any codes
     *      or radius doesn't have any codes.
     */
    public List<JoCode> getCodes(Species species, int radius) {
        return this.getCodes(species).getOrDefault(radius, new ArrayList<>());
    }

    /**
     * Gets a random code for the species given under the radius given.
     *
     * @param species The {@link Species} object to get the code for.
     * @param radius The radius of the code desired.
     * @param rand A {@link Random} instance.
     * @return The randomly selected {@link JoCode} object.
     */
    @Nullable
    public JoCode getRandomCode(Species species, int radius, Random rand) {
        final List<JoCode> list = this.getCodes(species, radius);

        if (list.isEmpty())
            return null;

        return list.get(rand.nextInt(list.size()));
    }

    @Override
    public CompletableFuture<Void> load(IResourceManager resourceManager) {
        return CompletableFuture.runAsync(() -> {});
    }

}
