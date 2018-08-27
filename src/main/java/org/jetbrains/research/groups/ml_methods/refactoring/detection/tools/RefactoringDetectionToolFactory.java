package org.jetbrains.research.groups.ml_methods.refactoring.detection.tools;

import org.jetbrains.annotations.NotNull;

public class RefactoringDetectionToolFactory {
    @NotNull
    public static RefactoringDetectionTool createRMiner() {
        return new RMiner();
    }
}
