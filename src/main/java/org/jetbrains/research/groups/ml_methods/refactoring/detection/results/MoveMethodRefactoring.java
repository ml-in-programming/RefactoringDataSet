package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class MoveMethodRefactoring {
    @NotNull
    private final MethodRefactoringInfo originalMethodInfo;
    @NotNull
    private final MethodRefactoringInfo movedMethodInfo;

    public MoveMethodRefactoring(@NotNull MethodRefactoringInfo originalMethodInfo,
                                 @NotNull MethodRefactoringInfo movedMethodInfo) {
        this.originalMethodInfo = originalMethodInfo;
        this.movedMethodInfo = movedMethodInfo;
    }

    @NotNull
    public MethodRefactoringInfo getOriginalMethodInfo() {
        return originalMethodInfo;
    }

    @NotNull
    public MethodRefactoringInfo getMovedMethodInfo() {
        return movedMethodInfo;
    }

    public static class MethodRefactoringInfo {
        @NotNull
        private final String classQualifiedName;

        @NotNull
        private final String methodName;

        @Nullable
        private final String returnType;

        @NotNull
        private final List<String> paramsClassesSimpleNames;

        @NotNull
        private final RefactoringFilePaths refactoringFilePaths;

        private final boolean isStatic;

        private final int originalStatementsCount;

        public MethodRefactoringInfo(@NotNull String classQualifiedName,
                                     @NotNull String methodName,
                                     @Nullable String returnType,
                                     @NotNull List<String> paramsClassesSimpleNames,
                                     @NotNull RefactoringFilePaths refactoringFilePaths,
                                     boolean isStatic,
                                     int originalStatementsCount) {
            this.classQualifiedName = classQualifiedName;
            this.methodName = methodName;
            this.returnType = returnType;
            this.paramsClassesSimpleNames = paramsClassesSimpleNames;
            this.refactoringFilePaths = refactoringFilePaths;
            this.isStatic = isStatic;
            this.originalStatementsCount = originalStatementsCount;
        }

        @NotNull
        public String getClassQualifiedName() {
            return classQualifiedName;
        }

        @NotNull
        public String getMethodName() {
            return methodName;
        }

        @Nullable
        public String getReturnType() {
            return returnType;
        }

        @NotNull
        public List<String> getParamsClassesSimpleNames() {
            return paramsClassesSimpleNames;
        }

        @NotNull
        public RefactoringFilePaths getRefactoringFilePaths() {
            return refactoringFilePaths;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public int getOriginalStatementsCount() {
            return originalStatementsCount;
        }

        public @NotNull String toShortString() {
            return classQualifiedName +
                    "." +
                    methodName +
                    "(" +
                    paramsClassesSimpleNames.stream().collect(Collectors.joining(", ")) +
                    ")";
        }

        public static class RefactoringFilePaths {
            @Nullable
            private final String filePathBefore;
            @Nullable
            private final String filePathAfter;

            public RefactoringFilePaths(@Nullable Path filePathBefore,
                                        @Nullable Path filePathAfter) {
                this.filePathBefore = filePathBefore == null ? null : filePathBefore.toString();
                this.filePathAfter = filePathAfter == null ? null : filePathAfter.toString();
            }

            @Nullable
            public String getFilePathBefore() {
                return filePathBefore;
            }

            @Nullable
            public String getFilePathAfter() {
                return filePathAfter;
            }
        }
    }

    public @NotNull String toShortString() {
        return originalMethodInfo.toShortString() + " -> " + movedMethodInfo.toShortString();
    }
}
