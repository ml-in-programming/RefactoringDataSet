package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommitDetectionSuccess extends CommitDetectionResult {
    @NotNull
    private final List<MoveMethodRefactoring> refactorings;

    public CommitDetectionSuccess(@NotNull String commitHash,
                                  @NotNull List<MoveMethodRefactoring> refactorings) {
        super(commitHash);
        this.refactorings = refactorings;
    }

    @NotNull
    public List<MoveMethodRefactoring> getRefactorings() {
        return refactorings;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }
}
