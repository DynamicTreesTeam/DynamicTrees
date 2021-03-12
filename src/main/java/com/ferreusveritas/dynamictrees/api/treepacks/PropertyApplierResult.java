package com.ferreusveritas.dynamictrees.api.treepacks;

/**
 * Stores an error message in the event of a property applier failure.
 *
 * @author Harley O'Connor
 */
public final class PropertyApplierResult {

    /** A successful property application. */
    public static final PropertyApplierResult SUCCESS = new PropertyApplierResult(null);

    /** Stores the error message, or null to signify there was none. */
    private final String errorMessage;

    public PropertyApplierResult (String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean wasSuccessful () {
        return this.errorMessage == null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
