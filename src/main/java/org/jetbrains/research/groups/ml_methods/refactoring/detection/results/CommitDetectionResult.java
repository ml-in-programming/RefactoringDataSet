package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

public abstract class CommitDetectionResult {
    @NotNull
    private final String commitHash;

    public CommitDetectionResult(
        final @NotNull String commitHash
    ) {
        this.commitHash = commitHash;
    }

    @NotNull
    public String getCommitHash() {
        return commitHash;
    }

    public abstract boolean isSuccess();
}
