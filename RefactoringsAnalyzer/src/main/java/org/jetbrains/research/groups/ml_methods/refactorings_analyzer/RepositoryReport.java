package org.jetbrains.research.groups.ml_methods.refactorings_analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.CommitDetectionSuccess;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.MoveMethodRefactoring;

import java.util.List;

public class RepositoryReport {
    private final @NotNull String repositoryName;

    private final int processedCommitsNum;

    private final int failedCommitsNum;

    private final int commitsWithMoveMethodRefactoringsNum;

    private final int detectedRefactoringsNum;

    private final int filteredCommitsNum;

    private final int filteredRefactoringsNum;

    private final @NotNull List<CommitDetectionSuccess> sampleCommits;

    public RepositoryReport(
        final @NotNull String repositoryName,
        final int processedCommitsNum,
        final int failedCommitsNum,
        final int commitsWithMoveMethodRefactoringsNum,
        final int detectedRefactoringsNum,
        final int filteredCommitsNum,
        final int filteredRefactoringsNum,
        final @NotNull List<CommitDetectionSuccess> sampleCommits
    ) {
        this.repositoryName = repositoryName;
        this.processedCommitsNum = processedCommitsNum;
        this.failedCommitsNum = failedCommitsNum;
        this.commitsWithMoveMethodRefactoringsNum = commitsWithMoveMethodRefactoringsNum;
        this.detectedRefactoringsNum = detectedRefactoringsNum;
        this.filteredCommitsNum = filteredCommitsNum;
        this.filteredRefactoringsNum = filteredRefactoringsNum;
        this.sampleCommits = sampleCommits;
    }

    public @NotNull String getRepositoryName() {
        return repositoryName;
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
        StringBuilder builder = new StringBuilder();

        builder.append(
            String.format(
                "%s\n" +
                "Processed commits: %s\n" +
                "Out of them failed %s\n" +
                "Out of them with move method refactorings: %s\n" +
                "Total number of detected refactorings: %s\n" +
                "Commits after filtration: %s\n" +
                "Refactorings after filtration: %s\n",
                repositoryName,
                processedCommitsNum,
                failedCommitsNum,
                commitsWithMoveMethodRefactoringsNum,
                detectedRefactoringsNum,
                filteredCommitsNum,
                filteredRefactoringsNum
            )
        );

        for (CommitDetectionSuccess commit : sampleCommits) {
            builder.append(commit.getCommitHash()).append('\n');
            for (MoveMethodRefactoring refactoring : commit.getRefactorings()) {
                builder.append(refactoring.toShortString()).append('\n');
            }
        }

        return builder.toString();
    }
}
