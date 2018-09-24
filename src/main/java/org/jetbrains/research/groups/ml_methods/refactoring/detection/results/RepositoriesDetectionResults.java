package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoriesDetectionResults {
    @NotNull
    private final List<RepositoryDetectionSuccess> succeededRepositories = new ArrayList<>();
    @NotNull
    private final List<RepositoryDetectionFailed> failedRepositories = new ArrayList<>();
    @NotNull
    private final ResultsStatistics statistics;

    RepositoriesDetectionResults(@NotNull List<RepositoryDetectionResult> repositoriesResults) {
        succeededRepositories.addAll(repositoriesResults.stream()
                .filter(repositoryDetectionResult -> repositoryDetectionResult instanceof RepositoryDetectionSuccess)
                .map(repositoryDetectionResult -> (RepositoryDetectionSuccess) repositoryDetectionResult)
                .collect(Collectors.toList()));
        failedRepositories.addAll(repositoriesResults.stream()
                .filter(repositoryDetectionResult -> repositoryDetectionResult instanceof RepositoryDetectionFailed)
                .map(repositoryDetectionResult -> (RepositoryDetectionFailed) repositoryDetectionResult)
                .collect(Collectors.toList()));
        final List<CommitDetectionSuccess> commitSuccesses = succeededRepositories.stream()
                .flatMap(repositoryDetectionSuccess ->
                        repositoryDetectionSuccess.getCommitDetectionSuccesses().stream())
                .collect(Collectors.toList());
        final List<CommitDetectionFailed> commitFailures = succeededRepositories.stream()
                .flatMap(repositoryDetectionSuccess ->
                        repositoryDetectionSuccess.getCommitDetectionFailures().stream())
                .collect(Collectors.toList());
        statistics = new ResultsStatistics(commitSuccesses, commitFailures);
    }

    public int getTotalRepositoriesNumber() {
        return getSuccessRepositoriesNumber() + getFailedRepositoriesNumber();
    }

    public int getSuccessRepositoriesNumber() {
        return succeededRepositories.size();
    }

    public int getFailedRepositoriesNumber() {
        return failedRepositories.size();
    }

    @Override
    public String toString() {
        return "Number of repositories: " + getTotalRepositoriesNumber() + "\n" +
                "Success: " + getSuccessRepositoriesNumber() + " / " + getTotalRepositoriesNumber() + "\n"
                + statistics;
    }
}
