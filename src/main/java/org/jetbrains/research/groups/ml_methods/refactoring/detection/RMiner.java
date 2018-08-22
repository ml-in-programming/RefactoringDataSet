package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

class RMiner implements RefactoringDetectionTool {
    @NotNull
    @Override
    public List<MoveMethodRefactoringFromVCS> detect(@NotNull URL repositoryUrl) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public List<MoveMethodRefactoringFromVCS> detect(@NotNull List<URL> repositoryUrls) {
        throw new UnsupportedOperationException();
    }
}
