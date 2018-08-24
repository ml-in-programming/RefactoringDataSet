package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.apache.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.Logging;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.refactoring.detection.RefactoringDetectionApplication.printExceptionInformation;

abstract class DefaultBranchesDetectionTool implements RefactoringDetectionTool {
    @NotNull
    private static final Logger LOGGER = Logging.getLogger(DefaultBranchesDetectionTool.class);

    @NotNull
    private static String getDefaultBranch(@NotNull URL repositoryUrl) throws IOException {
        RepositoryId repositoryId = RepositoryId.createFromUrl(ParsingUtils.getHttpLink(repositoryUrl));
        if (repositoryId == null) {
            throw new IllegalArgumentException(repositoryUrl + " repository url format is incorrect");
        }
        return new RepositoryService().getRepository(repositoryId.getOwner(), repositoryId.getName()).getMasterBranch();
    }

    @NotNull
    @Override
    public List<RepositoryDetectionResult> detect(@NotNull List<URL> repositoryUrls) {
        String passedProjects = repositoryUrls.stream()
                .map(ParsingUtils::getProjectName)
                .collect(Collectors.joining(", "));
        LOGGER.info("Started detection for projects: " + passedProjects);
        List<RepositoryDetectionResult> detected = new ArrayList<>();
        int passedRepositories = 0;
        for (URL repositoryUrl : repositoryUrls) {
            String branch = null;
            RepositoryDetectionResult detectedRefactorings;
            try {
                branch = getDefaultBranch(repositoryUrl);
                detectedRefactorings = detect(repositoryUrl, branch);
            } catch (Exception e) {
                String errorDescription = "Error occurred during refactoring detection for repository: " + repositoryUrl;
                LOGGER.error(errorDescription, e);
                System.err.println(errorDescription);
                printExceptionInformation(e);
                detectedRefactorings = new RepositoryDetectionFailed(repositoryUrl, branch, e);
            }
            detected.add(detectedRefactorings);
            passedRepositories++;
            System.out.println("Processed repositories: " + passedRepositories + " / " + repositoryUrls.size());
        }
        LOGGER.info("Ended detection for projects: " + passedProjects);
        return detected;
    }
}
