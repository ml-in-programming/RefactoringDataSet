package org.jetbrains.research.groups.ml_methods.refactoring.detection.tools;

import org.apache.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionFailed;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionResult;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionSuccess;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.ErrorReporter;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.ParsingUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class DefaultBranchesDetectionTool implements RefactoringDetectionTool {
    @NotNull
    private static final Logger LOGGER = Logger.getLogger(DefaultBranchesDetectionTool.class);

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
        return detect(repositoryUrls, null);
    }

    @NotNull
    @Override
    public List<RepositoryDetectionResult> detectAndSave(@NotNull List<URL> repositoryUrls,
                                                         @NotNull Path outputDirPath) {
        return detect(repositoryUrls, outputDirPath);
    }

    @NotNull
    private List<RepositoryDetectionResult> detect(@NotNull List<URL> repositoryUrls,
                                                   @Nullable Path outputDirPath) {
        String passedProjects = repositoryUrls.stream()
                .map(ParsingUtils::getProjectName)
                .collect(Collectors.joining(", "));
        LOGGER.info("Started detection for projects: " + passedProjects);
        List<RepositoryDetectionResult> detected = new ArrayList<>();
        int passedRepositories = 0;
        for (URL repositoryUrl : repositoryUrls) {
            String branch = null;
            RepositoryDetectionResult repositoryDetectionResult;
            try {
                branch = getDefaultBranch(repositoryUrl);
                repositoryDetectionResult = detect(repositoryUrl, branch);
            } catch (Exception e) {
                String errorMessage = "Error occurred during refactoring detection for repository: " + repositoryUrl;
                ErrorReporter.reportError(errorMessage, e, this.getClass());
                repositoryDetectionResult = new RepositoryDetectionFailed(repositoryUrl, branch, e);
            }

            if (outputDirPath != null) {
                if (repositoryDetectionResult instanceof RepositoryDetectionSuccess) {
                    String projectName = ParsingUtils.getProjectName(repositoryDetectionResult.getRepository());
                    Path outputFilePath = outputDirPath.resolve(projectName);
                    try {
                        repositoryDetectionResult.write(outputFilePath);
                    } catch (IOException e) {
                        String errorMessage = "Error occurred during writing to " + outputFilePath + " file.";
                        ErrorReporter.reportError(errorMessage, e, this.getClass());
                        System.err.println("Refactorings of " + projectName + " project can be corrupted.");
                    }
                }
            }

            detected.add(repositoryDetectionResult);
            passedRepositories++;
            System.out.println("Processed repositories: " + passedRepositories + " / " + repositoryUrls.size());
        }
        LOGGER.info("Ended detection for projects: " + passedProjects);
        return detected;
    }
}
