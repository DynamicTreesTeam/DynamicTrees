package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.util.json.FetchResult;

import java.util.Collections;
import java.util.List;

/**
 * Stores an error message in the event of a property applier failure.
 *
 * @author Harley O'Connor
 */
public final class PropertyApplierResult {

    /** Stores the error message, or null to signify there was none. */
    private String errorMessage;

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

    public PropertyApplierResult addErrorPrefix(final String prefix) {
        this.errorMessage = prefix + this.errorMessage;
        return this;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public PropertyApplierResult addWarningsPrefix(final String prefix) {
        this.warnings.forEach(warning -> warning = prefix + warning);
        return this;
    }

    public static PropertyApplierResult success () {
        return new PropertyApplierResult(null);
    }

    public static PropertyApplierResult success(final List<String> warnings) {
        return new PropertyApplierResult(null, warnings);
    }

    public static PropertyApplierResult failure(final FetchResult<?> fetchResult) {
        return new PropertyApplierResult(fetchResult.getErrorMessage(), fetchResult.getWarnings());
    }

    public static PropertyApplierResult failure(final String errorMessage) {
        return new PropertyApplierResult(errorMessage);
    }

    public static PropertyApplierResult failure(final String errorMessage, final List<String> warnings) {
        return new PropertyApplierResult(errorMessage, warnings);
    }

    public static PropertyApplierResult from(final FetchResult<?> fetchResult) {
        return fetchResult.wasSuccessful() ? success() : failure(fetchResult.getErrorMessage());
    }

}
