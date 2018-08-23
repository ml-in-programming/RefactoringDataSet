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

class DetectedRefactoringsInRepository implements Writable {
    @NotNull
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    @NotNull
    private final URL repository;
    @Nullable
    private final List<MoveMethodRefactoringFromVCS> detectedRefactorings;
    @Nullable
    private final Exception exception;
    @NotNull
    private String branch;

    DetectedRefactoringsInRepository(@NotNull URL repository,
                                     @NotNull String branch, @NotNull List<MoveMethodRefactoringFromVCS> detectedRefactorings) {
        this.repository = repository;
        this.branch = branch;
        this.detectedRefactorings = detectedRefactorings;
        this.exception = null;
    }

    DetectedRefactoringsInRepository(@NotNull URL repository,
                                     @NotNull String branch,
                                     @NotNull Exception exception) {
        this.repository = repository;
        this.branch = branch;
        this.detectedRefactorings = null;
        this.exception = exception;
    }

    @Override
    public void write(Path outputFilePath) throws IOException {
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

    @NotNull
    String getBranch() {
        return branch;
    }
}
