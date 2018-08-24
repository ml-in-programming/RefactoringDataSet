package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

class RepositoryDetectionResultCombiner {
    private final RepositoryRefactoringDetectionExecutionInfoCombiner executionInfoCombiner
            = new RepositoryRefactoringDetectionExecutionInfoCombiner();
    private int succeeded = 0;
    private int total = 0;

    RepositoryDetectionResultCombiner() {
    }

    @NotNull
    RepositoryDetectionResultCombiner add(RepositoryDetectionResult repositoryDetectionResult) {
        if (repositoryDetectionResult instanceof RepositoryDetectionSuccess) {
            succeeded++;
            executionInfoCombiner.add(((RepositoryDetectionSuccess) repositoryDetectionResult).getExecutionInfo());
        }
        total++;
        return this;
    }

    RepositoriesDetectionResults combine() {
        return new RepositoriesDetectionResults(succeeded, total, executionInfoCombiner.combine());
    }
}
