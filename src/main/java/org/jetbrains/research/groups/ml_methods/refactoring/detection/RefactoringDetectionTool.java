package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

public interface RefactoringDetectionTool {
    @NotNull
    RepositoryDetectedRefactorings detect(@NotNull URL repositoryUrl, @NotNull String branch) throws Exception;

    @NotNull
    List<RepositoryDetectedRefactorings> detect(@NotNull List<URL> repositoryUrls);
}
