package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RepositoryDetectionResultCombiner {
    private final List<RepositoryDetectionResult> repositoriesDetectionResults = new ArrayList<>();

    public RepositoryDetectionResultCombiner() {
    }

    @NotNull
    public RepositoryDetectionResultCombiner add(RepositoryDetectionResult repositoryDetectionResult) {
        repositoriesDetectionResults.add(repositoryDetectionResult);
        return this;
    }

    public RepositoriesDetectionResults combine() {
        return new RepositoriesDetectionResults(repositoriesDetectionResults);
    }
}
