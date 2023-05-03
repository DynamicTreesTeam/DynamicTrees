package com.ferreusveritas.dynamictrees.api.resource.loading.preparation;

import com.ferreusveritas.dynamictrees.api.resource.DTResource;
import com.ferreusveritas.dynamictrees.api.resource.ResourceCollector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TextResourcePreparer extends AbstractResourcePreparer<List<String>> {

    private static final String EXTENSION = ".txt";

    public TextResourcePreparer(String folder) {
        this(folder, ResourceCollector.unordered());
    }

    public TextResourcePreparer(String folderName, ResourceCollector<List<String>> resourceCollector) {
        super(folderName, EXTENSION, resourceCollector);
    }

    @Override
    protected void readAndPutResource(Resource resource, ResourceLocation resourceName)
            throws IOException {
        final List<String> lines = this.readResource(resource);
        this.resourceCollector.put(new DTResource<>(resourceName, lines));
    }

    private List<String> readResource(Resource resource) throws IOException {
        return this.readLines(resource.openAsReader(), new ArrayList<>());
    }

    private List<String> readLines(BufferedReader reader, List<String> lines) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            this.offerLine(lines, line);
        }
        return lines;
    }

    protected void offerLine(List<String> lines, String line) {
        lines.add(line);
    }

}