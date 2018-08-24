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
        return getRefactoringsNumbersInProcessedCommits().size();
    }

    double getMedianOfNotNullRefactoringsNumbers() {
        final List<Integer> refactoringsNumbersInProcessedCommits = getRefactoringsNumbersInProcessedCommits();
        if (refactoringsNumbersInProcessedCommits.size() == 0) {
            return Double.NaN;
        }
        int middleIndex = refactoringsNumbersInProcessedCommits.size() / 2;
        return refactoringsNumbersInProcessedCommits.size() % 2 == 0 ?
                (double) (refactoringsNumbersInProcessedCommits.get(middleIndex - 1) +
                        refactoringsNumbersInProcessedCommits.get(middleIndex)) / 2
                : refactoringsNumbersInProcessedCommits.get(middleIndex);
    }

    double getMaxCommitRefactoringsNumber() {
        final List<Integer> refactoringsNumbersInProcessedCommits = getRefactoringsNumbersInProcessedCommits();
        if (refactoringsNumbersInProcessedCommits.size() == 0) {
            return Double.NaN;
        }
        return refactoringsNumbersInProcessedCommits.get(refactoringsNumbersInProcessedCommits.size() - 1);
    }

    int getProcessedCommitsNumber() {
        return processedCommitsNumber;
    }

    @NotNull
    List<Integer> getRefactoringsNumbersInProcessedCommits() {
        return refactoringsNumbersInProcessedCommits;
    }

    @Override
    public String toString() {
        String out = "";
        out += "Processed commits: " + processedCommitsNumber + "\n";
        out += "Processed commits with refactorings: " + getProcessedCommitsWithRefactoringsNumber() + "\n";
        out += "Median of number of refactorings in commits " +
                "(only for commits that contain refactorings): " +
                getMedianOfNotNullRefactoringsNumbers() + "\n";
        out += "Max number of refactorings in one commit: " +
                getMaxCommitRefactoringsNumber() + "\n";
        out += "Detected refactorings: " +
                refactoringsNumbersInProcessedCommits.stream().mapToInt(Integer::intValue).sum();
        return out;
    }
}
