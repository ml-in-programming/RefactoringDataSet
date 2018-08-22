package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

class MoveMethodRefactoringFromVCS {
    private final URL repositoryURL;
    private final String commitHash;
    private final String targetClassQualifiedName;
    private final String sourceClassQualifiedName;
    private final String methodName;
    private final List<String> paramsClassesQualifiedNames;

    public MoveMethodRefactoringFromVCS(@NotNull URL repositoryURL,
                                        @NotNull String commitHash,
                                        @NotNull String targetClassQualifiedName,
                                        @NotNull String sourceClassQualifiedName,
                                        @NotNull String methodName,
                                        @NotNull List<String> paramsClassesQualifiedNames) {
        this.repositoryURL = repositoryURL;
        this.commitHash = commitHash;
        this.targetClassQualifiedName = targetClassQualifiedName;
        this.sourceClassQualifiedName = sourceClassQualifiedName;
        this.methodName = methodName;
        this.paramsClassesQualifiedNames = paramsClassesQualifiedNames;
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

    public String getSourceClassQualifiedName() {
        return sourceClassQualifiedName;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParamsClassesQualifiedNames() {
        return paramsClassesQualifiedNames;
    }
}
