package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

class MoveMethodRefactoringFromVCS {
    @NotNull
    private final URL repositoryURL;
    @NotNull
    private final String commitHash;
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

    @NotNull
    public List<String> getMovedParamsClassesQualifiedNames() {
        return movedParamsClassesQualifiedNames;
    }

    @NotNull
    public String getMovedMethodName() {
        return movedMethodName;
    }

    @NotNull
    public URL getRepositoryURL() {
        return repositoryURL;
    }

    @NotNull
    public String getCommitHash() {
        return commitHash;
    }

    @NotNull
    public String getTargetClassQualifiedName() {
        return targetClassQualifiedName;
    }

    @NotNull
    public String getOriginalClassQualifiedName() {
        return originalClassQualifiedName;
    }

    @NotNull
    public String getOriginalMethodName() {
        return originalMethodName;
    }

    @NotNull
    public List<String> getOriginalParamsClassesQualifiedNames() {
        return originalParamsClassesQualifiedNames;
    }
}
