package com.ferreusveritas.dynamictrees.api.resource.loading.preparation;

import com.ferreusveritas.dynamictrees.api.resource.Resource;
import com.ferreusveritas.dynamictrees.api.resource.ResourceCollector;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@link com.ferreusveritas.dynamictrees.api.resource.loading.ResourcePreparer} implementation that maps text file resources into a list of their lines.
 *
 * @author Harley O'Connor
 */
public class TextResourcePreparer extends AbstractResourcePreparer<List<String>> {

    private static final String EXTENSION = ".txt";

    public TextResourcePreparer(String folder) {
        this(folder, ResourceCollector.unordered());
    }

    public TextResourcePreparer(String folderName, ResourceCollector<List<String>> resourceCollector) {
        super(folderName, EXTENSION, resourceCollector);
    }

    @Override
    protected void readAndPutResource(IResource resource, ResourceLocation resourceName)
            throws IOException {
        final List<String> lines = this.readResource(resource);
        this.resourceCollector.put(new Resource<>(resourceName, lines));
    }

    private List<String> readResource(IResource resource) throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        );
        return this.readLines(reader, new ArrayList<>());
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
