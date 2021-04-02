package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;

import java.util.Collections;
import java.util.List;

/**
 * Stores an error message in the event of a property applier failure.
 *
 * @author Harley O'Connor
 */
public final class PropertyApplierResult {

    /** Stores the error message, or null to signify there was none. */
    private final String errorMessage;

    /** Stores any warnings. */
    private final List<String> warnings;

    private PropertyApplierResult (final String errorMessage) {
        this(errorMessage, Collections.emptyList());
    }

    private PropertyApplierResult(final String errorMessage, final List<String> warnings) {
        this.errorMessage = errorMessage;
        this.warnings = warnings;
    }

    public boolean wasSuccessful () {
        return this.errorMessage == null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public static PropertyApplierResult success () {
        return new PropertyApplierResult(null);
    }

    public static PropertyApplierResult success(final List<String> warnings) {
        return new PropertyApplierResult(null, warnings);
    }

    public static PropertyApplierResult failure(final ObjectFetchResult<?> fetchResult) {
        return new PropertyApplierResult(fetchResult.getErrorMessage(), fetchResult.getWarnings());
    }

    public static PropertyApplierResult failure(final String errorMessage) {
        return new PropertyApplierResult(errorMessage);
    }

    public static PropertyApplierResult failure(final String errorMessage, final List<String> warnings) {
        return new PropertyApplierResult(errorMessage, warnings);
    }

}
