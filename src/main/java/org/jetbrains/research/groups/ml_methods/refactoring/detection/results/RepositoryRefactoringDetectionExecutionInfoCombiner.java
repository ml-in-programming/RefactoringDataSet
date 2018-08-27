package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class RepositoryRefactoringDetectionExecutionInfoCombiner {
    @NotNull
    private final List<Integer> refactoringsNumbersInProcessedCommits;
    private int processedCommitsNumber;

    RepositoryRefactoringDetectionExecutionInfoCombiner() {
        processedCommitsNumber = 0;
        refactoringsNumbersInProcessedCommits = new ArrayList<>();
    }

    @NotNull
    RepositoryRefactoringDetectionExecutionInfoCombiner add(RefactoringDetectionExecutionInfo executionInfo) {
        processedCommitsNumber += executionInfo.getProcessedCommitsNumber();
        refactoringsNumbersInProcessedCommits.addAll(executionInfo.getRefactoringsNumbersInProcessedCommits());
        return this;
    }

    RefactoringDetectionExecutionInfo combine() {
        return new RefactoringDetectionExecutionInfo(processedCommitsNumber, refactoringsNumbersInProcessedCommits);
    }
}
