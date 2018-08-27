package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoveMethodCommitRefactorings {
    @NotNull
    private final String commitHash;
    @NotNull
    private final List<MoveMethodRefactoring> refactorings;

    public MoveMethodCommitRefactorings(@NotNull String commitHash,
                                        @NotNull List<MoveMethodRefactoring> refactorings) {
        this.commitHash = commitHash;
        this.refactorings = refactorings;
    }
}
