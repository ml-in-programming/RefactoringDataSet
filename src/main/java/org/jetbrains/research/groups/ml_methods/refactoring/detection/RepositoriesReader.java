package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class RepositoriesReader {
    @NotNull
    private static final Logger LOGGER = Logger.getLogger(RepositoriesReader.class);

    @NotNull
    static List<URL> read(@NotNull Path repositoriesFilePath) throws IOException {
        LOGGER.info("Started parsing repository urls file.");
        List<String> repositoryUrls = Files.lines(repositoriesFilePath).collect(Collectors.toList());
        List<URL> repositories = new ArrayList<>();
        for (String repositoryUrl : repositoryUrls) {
            repositories.add(new URL(repositoryUrl));
            LOGGER.info("Parsed " + repositoryUrl);
        }
        return repositories;
    }
}
