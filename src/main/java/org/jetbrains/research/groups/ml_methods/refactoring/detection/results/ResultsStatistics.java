package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ResultsStatistics {
    @NotNull
    private final List<CommitDetectionSuccess> commitDetectionSuccesses;
    @NotNull
    private final List<CommitDetectionFailed> commitDetectionFailures;

    public ResultsStatistics(@NotNull List<CommitDetectionSuccess> commitDetectionSuccesses,
                             @NotNull List<CommitDetectionFailed> commitDetectionFailures) {
        this.commitDetectionSuccesses = commitDetectionSuccesses;
        this.commitDetectionFailures = commitDetectionFailures;
    }

    public int getTotalCommitsNumber() {
        return getSucceededCommitsNumber() + getFailedCommitsNumber();
    }

    public int getSucceededCommitsNumber() {
        return commitDetectionSuccesses.size();
    }

    public int getFailedCommitsNumber() {
        return commitDetectionFailures.size();
    }

    public double getMedianOfNotNullRefactoringsNumbers() {
        final List<Integer> refactoringsNumbersInProcessedCommits = getSortedRefactoringsNumbersInCommits();
        if (refactoringsNumbersInProcessedCommits.size() == 0) {
            return Double.NaN;
        }
        int middleIndex = refactoringsNumbersInProcessedCommits.size() / 2;
        return refactoringsNumbersInProcessedCommits.size() % 2 == 0 ?
                (double) (refactoringsNumbersInProcessedCommits.get(middleIndex - 1) +
                        refactoringsNumbersInProcessedCommits.get(middleIndex)) / 2
                : refactoringsNumbersInProcessedCommits.get(middleIndex);
    }

    public double getMaxCommitRefactoringsNumber() {
        final List<Integer> refactoringsNumbersInProcessedCommits = getSortedRefactoringsNumbersInCommits();
        if (refactoringsNumbersInProcessedCommits.size() == 0) {
            return Double.NaN;
        }
        return refactoringsNumbersInProcessedCommits.get(refactoringsNumbersInProcessedCommits.size() - 1);
    }

    private List<Integer> getSortedRefactoringsNumbersInCommits() {
        return commitDetectionSuccesses.stream()
                .map(value -> value.getRefactorings().size())
                .filter(integer -> integer > 0)
                .sorted()
                .collect(Collectors.toList());
    }

    public int getCommitsWithRefactoringsNumber() {
        return (int) commitDetectionSuccesses.stream()
                .filter(commitDetectionSuccess -> commitDetectionSuccess.getRefactorings().size() > 0)
                .count();
    }

    @Override
    public String toString() {
        String out = "";
        out += "Success commits: " + getSucceededCommitsNumber() + " / " + getTotalCommitsNumber() + "\n";
        out += "Processed commits with refactorings: " + getCommitsWithRefactoringsNumber() + "\n";
        out += "Median of number of refactorings in commits " +
                "(only for commits that contain refactorings): " +
                getMedianOfNotNullRefactoringsNumbers() + "\n";
        out += "Max number of refactorings in one commit: " +
                getMaxCommitRefactoringsNumber() + "\n";
        out += "Detected refactorings: " +
                commitDetectionSuccesses.stream().mapToInt(value -> value.getRefactorings().size()).sum();
        return out;
    }
}
