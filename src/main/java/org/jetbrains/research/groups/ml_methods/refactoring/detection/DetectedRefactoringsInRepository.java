package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class DetectedRefactoringsInRepository implements Writable {
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    private final URL repository;
    private final List<MoveMethodRefactoringFromVCS> detectedRefactorings;

    DetectedRefactoringsInRepository(URL repository, List<MoveMethodRefactoringFromVCS> detectedRefactorings) {
        this.repository = repository;
        this.detectedRefactorings = detectedRefactorings;
    }

    @Override
    public void write(Path outputFilePath) throws IOException {
        Files.write(outputFilePath, Collections.singleton(JSON_CONVERTER.toJson(this)));
    }

    URL getRepository() {
        return repository;
    }

    List<MoveMethodRefactoringFromVCS> getDetectedRefactorings() {
        return detectedRefactorings;
    }
}
