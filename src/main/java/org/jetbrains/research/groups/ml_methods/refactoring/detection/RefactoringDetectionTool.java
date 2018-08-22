package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

public interface RefactoringDetectionTool {
    @NotNull
    List<MoveMethodRefactoringFromVCS> detect(@NotNull URL repositoryUrl);

    @NotNull
    List<MoveMethodRefactoringFromVCS> detect(@NotNull List<URL> repositoryUrls);
}
