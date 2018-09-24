package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.ParsingUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public abstract class RepositoryDetectionResult {
    @NotNull
    private static final Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    @NotNull
    private final URL repository;
    @Nullable
    private String branch;

    RepositoryDetectionResult(@NotNull URL repository, @Nullable String branch) {
        this.repository = repository;
        this.branch = branch;
    }

    public void write(Path outputFilePath) throws IOException {
        outputFilePath.getParent().toFile().mkdirs();
        Files.write(outputFilePath, Collections.singleton(JSON_CONVERTER.toJson(this)));
    }

    @NotNull
    public URL getRepository() {
        return repository;
    }

    @Override
    public String toString() {
        return "Repository url: " + repository + "\n" +
                "Project: " + ParsingUtils.getProjectName(repository) + "\n" +
                "Branch: " + branch + "\n";
    }
}
