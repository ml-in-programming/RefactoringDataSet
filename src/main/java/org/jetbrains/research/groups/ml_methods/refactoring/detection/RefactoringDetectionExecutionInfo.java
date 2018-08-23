package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class RefactoringDetectionExecutionInfo {
    private final int processedCommitsNumber;
    @NotNull
    private final List<Integer> refactoringsNumbersInProcessedCommits;

    RefactoringDetectionExecutionInfo(int processedCommitsNumber,
                                      @NotNull List<Integer> refactoringsNumbersInProcessedCommits) {
        this.processedCommitsNumber = processedCommitsNumber;
        this.refactoringsNumbersInProcessedCommits = refactoringsNumbersInProcessedCommits;
        Collections.sort(this.refactoringsNumbersInProcessedCommits);
    }

    int getProcessedCommitsWithRefactoringsNumber() {
        return refactoringsNumbersInProcessedCommits.size();
    }

    int getProcessedCommitsNumber() {
        return processedCommitsNumber;
    }

    double getMedianOfNotNullRefactoringsNumbers() {
        if (refactoringsNumbersInProcessedCommits.size() == 0) {
            return Double.NaN;
        }
        int middleIndex = refactoringsNumbersInProcessedCommits.size() / 2;
        return refactoringsNumbersInProcessedCommits.size() % 2 == 0 ?
                (double) (refactoringsNumbersInProcessedCommits.get(middleIndex - 1) +
                        refactoringsNumbersInProcessedCommits.get(middleIndex)) / 2
                : refactoringsNumbersInProcessedCommits.get(middleIndex);
    }
}
