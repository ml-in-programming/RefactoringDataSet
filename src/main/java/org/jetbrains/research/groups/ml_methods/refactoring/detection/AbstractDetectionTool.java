package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.Logging;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.refactoring.detection.RefactoringDetectionApplication.printExceptionInformation;

abstract class AbstractDetectionTool implements RefactoringDetectionTool {
    private static final Logger LOGGER = Logging.getLogger(AbstractDetectionTool.class);
    private static final String DEFAULT_BRANCH = "master";

    @NotNull
    @Override
    public List<DetectedRefactoringsInRepository> detect(@NotNull List<URL> repositoryUrls, String branch) {
        String passedProjects = repositoryUrls.stream()
                .map(ParsingUtils::getProjectName)
                .collect(Collectors.joining(", "));
        LOGGER.info("Started detection for projects: " + passedProjects);
        List<DetectedRefactoringsInRepository> detected = new ArrayList<>();
        int passedRepositories = 0;
        for (URL repositoryUrl : repositoryUrls) {
            System.out.println("Processed repositories: " + passedRepositories++ + " / " + repositoryUrls.size());
            DetectedRefactoringsInRepository detectedRefactorings;
            try {
                detectedRefactorings = detect(repositoryUrl, branch);
            } catch (Exception e) {
                String errorDescription = "Error occurred during refactoring detection for repository: " + repositoryUrl;
                LOGGER.error(errorDescription, e);
                System.err.println(errorDescription);
                printExceptionInformation(e);
                detectedRefactorings = new DetectedRefactoringsInRepository(repositoryUrl, branch, e);
            }
            detected.add(detectedRefactorings);
        }
        LOGGER.info("Ended detection for projects: " + passedProjects);
        return detected;
    }

    @NotNull
    @Override
    public DetectedRefactoringsInRepository detect(@NotNull URL repositoryUrl) throws Exception {
        return detect(repositoryUrl, DEFAULT_BRANCH);
    }

    @NotNull
    @Override
    public List<DetectedRefactoringsInRepository> detect(@NotNull List<URL> repositoryUrls) {
        return detect(repositoryUrls, DEFAULT_BRANCH);
    }
}
