package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

public class RepositoriesDetectionResults {
    private final int succeededRepositories;
    private final int totalRepositoriesNumber;
    @NotNull
    private final RefactoringDetectionExecutionInfo combinedExecutionInfo;

    RepositoriesDetectionResults(int succeededRepositories, int totalRepositoriesNumber,
                                 @NotNull RefactoringDetectionExecutionInfo combinedExecutionInfo) {

        this.succeededRepositories = succeededRepositories;
        this.totalRepositoriesNumber = totalRepositoriesNumber;
        this.combinedExecutionInfo = combinedExecutionInfo;
    }

    @Override
    public String toString() {
        return "Number of repositories: " + totalRepositoriesNumber + "\n" +
                "Success: " + succeededRepositories + " / " + totalRepositoriesNumber + "\n"
                + combinedExecutionInfo;
    }
}
