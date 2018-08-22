package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

class MoveMethodRefactoringFromVCS {
    private final URL repositoryURL;
    private final String commitHash;
    private final String targetClassQualifiedName;
    private final String originalClassQualifiedName;
    private final String originalMethodName;
    private final String movedMethodName;
    private final List<String> originalParamsClassesQualifiedNames;
    private final List<String> movedParamsClassesQualifiedNames;

    MoveMethodRefactoringFromVCS(@NotNull URL repositoryURL,
                                 @NotNull String commitHash,
                                 @NotNull String targetClassQualifiedName,
                                 @NotNull String originalClassQualifiedName,
                                 @NotNull String originalMethodName,
                                 @NotNull String movedMethodName,
                                 @NotNull List<String> originalParamsClassesQualifiedNames,
                                 @NotNull List<String> movedParamsClassesQualifiedNames) {
        this.repositoryURL = repositoryURL;
        this.commitHash = commitHash;
        this.targetClassQualifiedName = targetClassQualifiedName;
        this.originalClassQualifiedName = originalClassQualifiedName;
        this.originalMethodName = originalMethodName;
        this.movedMethodName = movedMethodName;
        this.originalParamsClassesQualifiedNames = originalParamsClassesQualifiedNames;
        this.movedParamsClassesQualifiedNames = movedParamsClassesQualifiedNames;
    }

    public List<String> getMovedParamsClassesQualifiedNames() {
        return movedParamsClassesQualifiedNames;
    }

    public String getMovedMethodName() {
        return movedMethodName;
    }

    public URL getRepositoryURL() {
        return repositoryURL;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getTargetClassQualifiedName() {
        return targetClassQualifiedName;
    }

    public String getOriginalClassQualifiedName() {
        return originalClassQualifiedName;
    }

    public String getOriginalMethodName() {
        return originalMethodName;
    }

    public List<String> getOriginalParamsClassesQualifiedNames() {
        return originalParamsClassesQualifiedNames;
    }
}
