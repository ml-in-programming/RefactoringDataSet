package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public class RepositoryDetectionFailed extends RepositoryDetectionResult {
    @NotNull
    private final Exception exception;

    public RepositoryDetectionFailed(@NotNull URL repository, @Nullable String branch, @NotNull Exception exception) {
        super(repository, branch);
        this.exception = exception;
    }

    @Override
    public String toString() {
        return super.toString() + "Result: failed\n" +
                "Error type: " + exception.getClass().getCanonicalName() + "\n" +
                "Reason: " + exception.getMessage();
    }

    @NotNull
    Exception getException() {
        return exception;
    }
}
