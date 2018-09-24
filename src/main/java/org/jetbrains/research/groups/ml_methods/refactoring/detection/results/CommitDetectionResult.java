package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

public abstract class CommitDetectionResult {
    @NotNull
    private final String commitHash;

    public CommitDetectionResult(@NotNull String commitHash) {
        this.commitHash = commitHash;
    }

    public abstract boolean isSuccess();
}
