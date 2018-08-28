package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

public class CommitDetectionFailed extends CommitDetectionResult {
    @NotNull
    private final Exception exception;

    public CommitDetectionFailed(@NotNull String commitHash, @NotNull Exception exception) {
        super(commitHash);
        this.exception = exception;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }
}
