package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.TextResourcePreparer;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import java.util.List;

/**
 * Extension of {@link TextResourcePreparer} which reads the basic syntax used to store {@linkplain JoCode JoCodes} in a
 * text file. This includes ignoring lines that are too short to contain JoCodes, removing whitespace, and
 * ignoring/removing comments.
 *
 * @author Harley O'Connor
 */
public final class JoCodeResourcePreparer extends TextResourcePreparer {

    public JoCodeResourcePreparer(String folder) {
        super(folder);
    }

    @Override
    protected void offerLine(List<String> lines, String line) {
        if (this.shouldAddLine(line)) {
            lines.add(this.processLine(line));
        }
    }

    private boolean shouldAddLine(String line) {
        return (line.length() >= 3) && (line.charAt(0) != '#');
    }

    private String processLine(String line) {
        line = this.withoutWhitespace(line);
        return this.withoutTrailingComments(line);
    }

    private String withoutWhitespace(String line) {
        return line.replace(" ", "");
    }

    private String withoutTrailingComments(String line) {
        final int commentIndex = line.indexOf('#');
        return line.substring(0, commentIndex < 0 ? line.length() : commentIndex);
    }

}
