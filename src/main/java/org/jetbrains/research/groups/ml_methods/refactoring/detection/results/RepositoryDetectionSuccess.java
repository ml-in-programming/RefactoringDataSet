package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

public class RepositoryDetectionSuccess extends RepositoryDetectionResult {
    @NotNull
    private final RefactoringDetectionExecutionInfo executionInfo;
    @NotNull
    private final List<MoveMethodCommitRefactorings> detectedRefactorings;

    public RepositoryDetectionSuccess(@NotNull URL repository,
                                      @NotNull String branch,
                                      @NotNull RefactoringDetectionExecutionInfo executionInfo,
                                      @NotNull List<MoveMethodCommitRefactorings> detectedRefactorings) {
        super(repository, branch);
        this.detectedRefactorings = detectedRefactorings;
        this.executionInfo = executionInfo;
    }

    @Override
    public String toString() {
        return super.toString() + "Result: success\n" + executionInfo;
    }

    @NotNull
    public List<MoveMethodCommitRefactorings> getDetectedRefactorings() {
        return detectedRefactorings;
    }

    @NotNull
    public RefactoringDetectionExecutionInfo getExecutionInfo() {
        return executionInfo;
    }
}
