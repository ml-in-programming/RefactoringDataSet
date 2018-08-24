package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

class RepositoryDetectionSuccess extends RepositoryDetectionResult {
    @NotNull
    private final RefactoringDetectionExecutionInfo executionInfo;
    @NotNull
    private final List<MoveMethodRefactoringFromVCS> detectedRefactorings;

    RepositoryDetectionSuccess(@NotNull URL repository,
                               @NotNull String branch,
                               @NotNull RefactoringDetectionExecutionInfo executionInfo,
                               @NotNull List<MoveMethodRefactoringFromVCS> detectedRefactorings) {
        super(repository, branch);
        this.detectedRefactorings = detectedRefactorings;
        this.executionInfo = executionInfo;
    }

    @Override
    public String toString() {
        return super.toString() + "Result: success\n" + executionInfo;
    }

    @NotNull
    List<MoveMethodRefactoringFromVCS> getDetectedRefactorings() {
        return detectedRefactorings;
    }

    @NotNull
    RefactoringDetectionExecutionInfo getExecutionInfo() {
        return executionInfo;
    }
}
