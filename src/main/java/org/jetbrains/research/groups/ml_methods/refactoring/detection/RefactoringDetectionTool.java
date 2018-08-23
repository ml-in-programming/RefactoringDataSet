package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

public interface RefactoringDetectionTool {
    @NotNull
    DetectedRefactoringsInRepository detect(@NotNull URL repositoryUrl, @NotNull String branch) throws Exception;

    @NotNull
    List<DetectedRefactoringsInRepository> detect(@NotNull List<URL> repositoryUrls);
}
