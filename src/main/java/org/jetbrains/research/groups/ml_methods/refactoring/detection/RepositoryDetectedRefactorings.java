package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class RepositoryDetectedRefactorings {
    @NotNull
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    @NotNull
    private final URL repository;
    @Nullable
    private final RefactoringDetectionExecutionInfo executionInfo;
    @Nullable
    private String branch;
    @Nullable
    private final List<MoveMethodRefactoringFromVCS> detectedRefactorings;
    @Nullable
    private final Exception exception;

    RepositoryDetectedRefactorings(@NotNull URL repository,
                                   @NotNull String branch,
                                   @NotNull RefactoringDetectionExecutionInfo executionInfo,
                                   @NotNull List<MoveMethodRefactoringFromVCS> detectedRefactorings) {
        this.repository = repository;
        this.branch = branch;
        this.detectedRefactorings = detectedRefactorings;
        this.executionInfo = executionInfo;
        this.exception = null;
    }

    RepositoryDetectedRefactorings(@NotNull URL repository,
                                   @Nullable String branch,
                                   @NotNull Exception exception) {
        this.repository = repository;
        this.branch = branch;
        this.executionInfo = null;
        this.detectedRefactorings = null;
        this.exception = exception;
    }

    void write(Path outputFilePath) throws IOException {
        outputFilePath.getParent().toFile().mkdirs();
        Files.write(outputFilePath, Collections.singleton(JSON_CONVERTER.toJson(this)));
    }

    @NotNull
    URL getRepository() {
        return repository;
    }

    @Nullable
    List<MoveMethodRefactoringFromVCS> getDetectedRefactorings() {
        return detectedRefactorings;
    }

    boolean isSuccess() {
        return exception == null;
    }

    @Nullable
    Exception getException() {
        return exception;
    }

    @Nullable
    String getBranch() {
        return branch;
    }

    @Nullable
    RefactoringDetectionExecutionInfo getExecutionInfo() {
        return executionInfo;
    }
}
