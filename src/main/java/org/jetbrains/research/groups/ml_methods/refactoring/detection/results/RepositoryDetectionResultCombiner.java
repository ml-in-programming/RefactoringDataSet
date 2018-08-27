package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

public class RepositoryDetectionResultCombiner {
    private final RepositoryRefactoringDetectionExecutionInfoCombiner executionInfoCombiner
            = new RepositoryRefactoringDetectionExecutionInfoCombiner();
    private int succeeded = 0;
    private int total = 0;

    public RepositoryDetectionResultCombiner() {
    }

    @NotNull
    public RepositoryDetectionResultCombiner add(RepositoryDetectionResult repositoryDetectionResult) {
        if (repositoryDetectionResult instanceof RepositoryDetectionSuccess) {
            succeeded++;
            executionInfoCombiner.add(((RepositoryDetectionSuccess) repositoryDetectionResult).getExecutionInfo());
        }
        total++;
        return this;
    }

    public RepositoriesDetectionResults combine() {
        return new RepositoriesDetectionResults(succeeded, total, executionInfoCombiner.combine());
    }
}
