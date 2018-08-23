package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

class RefactoringDetectionToolFactory {
    @NotNull
    static RefactoringDetectionTool createRMiner() {
        return new RMiner();
    }
}
