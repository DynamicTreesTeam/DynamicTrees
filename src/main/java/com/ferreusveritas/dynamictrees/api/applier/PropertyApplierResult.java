package com.ferreusveritas.dynamictrees.api.applier;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

/**
 * Stores an error and warning messages resulting from {@link Applier#apply(Object, Object)}.
 *
 * @author Harley O'Connor
 */
public final class PropertyApplierResult {

    /**
     * The error message, or null to signify that there was none.
     */
    private String errorMessage;

    private final List<String> warnings;

    private PropertyApplierResult(final String errorMessage) {
        this(errorMessage, Lists.newLinkedList());
    }

    private PropertyApplierResult(final String errorMessage, final List<String> warnings) {
        this.errorMessage = errorMessage;
        this.warnings = warnings;
    }

    public boolean wasSuccessful() {
        return this.errorMessage == null;
    }

    public Optional<String> getError() {
        return Optional.ofNullable(errorMessage);
    }

    public PropertyApplierResult addErrorPrefix(final String prefix) {
        this.errorMessage = prefix + this.errorMessage;
        return this;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addWarnings(List<String> warning) {
        this.warnings.addAll(warning);
    }

    public PropertyApplierResult addWarningsPrefix(final String prefix) {
        this.warnings.replaceAll(warning -> warning = prefix + warning);
        return this;
    }

    public static PropertyApplierResult success() {
        return new PropertyApplierResult(null);
    }

    public static PropertyApplierResult success(final List<String> warnings) {
        return new PropertyApplierResult(null, warnings);
    }

    public static PropertyApplierResult failure(final String errorMessage) {
        return new PropertyApplierResult(errorMessage);
    }

    public static PropertyApplierResult failure(final String errorMessage, final List<String> warnings) {
        return new PropertyApplierResult(errorMessage, warnings);
    }

    public static PropertyApplierResult from(final Result<?, ?> result) {
        return result.success() ? success(result.getWarnings()) : failure(result.getError(), result.getWarnings());
    }

}
