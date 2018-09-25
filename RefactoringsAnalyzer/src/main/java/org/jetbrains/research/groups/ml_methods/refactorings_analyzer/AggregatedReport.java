package org.jetbrains.research.groups.ml_methods.refactorings_analyzer;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AggregatedReport {
    private final int processedCommitsNum;

    private final int failedCommitsNum;

    private final int commitsWithMoveMethodRefactoringsNum;

    private final int detectedRefactoringsNum;

    private final int filteredCommitsNum;

    private final int filteredRefactoringsNum;

    public AggregatedReport(final @NotNull List<RepositoryReport> reports) {
        processedCommitsNum =
            reports.stream().mapToInt(RepositoryReport::getProcessedCommitsNum).sum();

        failedCommitsNum =
            reports.stream().mapToInt(RepositoryReport::getFailedCommitsNum).sum();

        commitsWithMoveMethodRefactoringsNum =
            reports.stream().mapToInt(RepositoryReport::getCommitsWithMoveMethodRefactoringsNum).sum();

        detectedRefactoringsNum =
            reports.stream().mapToInt(RepositoryReport::getDetectedRefactoringsNum).sum();

        filteredCommitsNum =
            reports.stream().mapToInt(RepositoryReport::getFilteredCommitsNum).sum();

        filteredRefactoringsNum =
            reports.stream().mapToInt(RepositoryReport::getFilteredRefactoringsNum).sum();
    }

    public int getProcessedCommitsNum() {
        return processedCommitsNum;
    }

    public int getFailedCommitsNum() {
        return failedCommitsNum;
    }

    public int getCommitsWithMoveMethodRefactoringsNum() {
        return commitsWithMoveMethodRefactoringsNum;
    }

    public int getDetectedRefactoringsNum() {
        return detectedRefactoringsNum;
    }

    public int getFilteredCommitsNum() {
        return filteredCommitsNum;
    }

    public int getFilteredRefactoringsNum() {
        return filteredRefactoringsNum;
    }

    @Override
    public String toString() {
        return String.format(
            "Processed commits: %s\n" +
            "Out of them failed %s\n" +
            "Out of them with move method refactorings: %s\n" +
            "Total number of detected refactorings: %s\n" +
            "Commits after filtration: %s\n" +
            "Refactorings after filtration: %s\n",
            processedCommitsNum,
            failedCommitsNum,
            commitsWithMoveMethodRefactoringsNum,
            detectedRefactoringsNum,
            filteredCommitsNum,
            filteredRefactoringsNum
        );
    }
}
