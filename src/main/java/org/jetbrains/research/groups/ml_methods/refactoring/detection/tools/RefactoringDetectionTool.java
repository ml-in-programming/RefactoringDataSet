package org.jetbrains.research.groups.ml_methods.refactoring.detection.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionResult;

import java.net.URL;
import java.util.List;

public interface RefactoringDetectionTool {
    @NotNull
    RepositoryDetectionResult detect(@NotNull URL repositoryUrl, @NotNull String branch) throws Exception;

    @NotNull
    List<RepositoryDetectionResult> detect(@NotNull List<URL> repositoryUrls);
}
