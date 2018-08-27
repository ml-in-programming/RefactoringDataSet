package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class MoveMethodRefactoring {
    @NotNull
    private final String targetClassQualifiedName;
    @NotNull
    private final String originalClassQualifiedName;
    @NotNull
    private final String originalMethodName;
    @NotNull
    private final String movedMethodName;
    @NotNull
    private final List<String> originalParamsClassesQualifiedNames;
    @NotNull
    private final List<String> movedParamsClassesQualifiedNames;
    @NotNull
    private final RefactoringFilePaths refactoringsFilePathsHolder;

    public MoveMethodRefactoring(@NotNull String targetClassQualifiedName,
                                 @NotNull String originalClassQualifiedName,
                                 @NotNull String originalMethodName,
                                 @NotNull String movedMethodName,
                                 @NotNull List<String> originalParamsClassesQualifiedNames,
                                 @NotNull List<String> movedParamsClassesQualifiedNames,
                                 @NotNull RefactoringFilePaths refactoringsFilePathsHolder) {
        this.targetClassQualifiedName = targetClassQualifiedName;
        this.originalClassQualifiedName = originalClassQualifiedName;
        this.originalMethodName = originalMethodName;
        this.movedMethodName = movedMethodName;
        this.originalParamsClassesQualifiedNames = originalParamsClassesQualifiedNames;
        this.movedParamsClassesQualifiedNames = movedParamsClassesQualifiedNames;
        this.refactoringsFilePathsHolder = refactoringsFilePathsHolder;
    }

    public static class RefactoringFilePaths {
        @NotNull
        private final String originalFilePathsBefore;
        @Nullable
        private final String movedFilePathsBefore;
        @Nullable
        private final String originalFilePathsAfter;
        @NotNull
        private final String movedFilePathsAfter;

        public RefactoringFilePaths(@NotNull Path originalFilePathsBefore,
                                    @Nullable Path movedFilePathsBefore,
                                    @Nullable Path originalFilePathsAfter,
                                    @NotNull Path movedFilePathsAfter) {
            this.originalFilePathsBefore = originalFilePathsBefore.toString();
            this.movedFilePathsBefore = movedFilePathsBefore == null ? null : movedFilePathsBefore.toString();
            this.originalFilePathsAfter = originalFilePathsAfter == null ? null : originalFilePathsAfter.toString();
            this.movedFilePathsAfter = movedFilePathsAfter.toString();
        }
    }
}
