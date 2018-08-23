package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.Logging;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.lang.System.exit;

public class RefactoringDetectionApplication {
    @NotNull
    private static final String DEFAULT_DETECTION_TOOL_NAME = "RMiner";
    @NotNull
    private static final Logger LOGGER = Logging.getLogger(RefactoringDetectionApplication.class);

    public static void main(@NotNull String[] args) {
        if (args.length != 3 && args.length != 2) {
            printUsage();
            return;
        }

        Path pathToRepositoriesFile = Paths.get(args[0]);
        Path outputDirPath = Paths.get(args[1]);
        String detectionToolName = args.length == 3 ? args[2] : DEFAULT_DETECTION_TOOL_NAME;
        RefactoringDetectionTool refactoringDetectionTool;
        switch (detectionToolName) {
            case "RMiner":
                refactoringDetectionTool = RefactoringDetectionToolFactory.createRMiner();
                break;
            default:
                System.out.println("No such tool. Try again.");
                printUsage();
                return;
        }
        List<URL> repositories;
        try {
            repositories = RepositoriesReader.read(pathToRepositoriesFile);
        } catch (IOException e) {
            String errorDescription = "Error occurred during parsing passed repository urls.";
            exitWithError(errorDescription, e);
            return;
        }
        List<RepositoryDetectedRefactorings> detectedRefactorings;
        try {
            detectedRefactorings = refactoringDetectionTool.detect(repositories);
        } catch (Exception e) {
            String errorDescription = "Error occurred during refactoring detection.";
            exitWithError(errorDescription, e);
            return;
        }
        for (RepositoryDetectedRefactorings repositoryDetectedRefactorings : detectedRefactorings) {
            String projectName = ParsingUtils.getProjectName(repositoryDetectedRefactorings.getRepository());
            Path outputFilePath = outputDirPath.resolve(projectName);
            try {
                repositoryDetectedRefactorings.write(outputFilePath);
            } catch (IOException e) {
                System.err.println("Error occurred during writing to " + outputFilePath + " file.");
                printExceptionInformation(e);
                System.err.println("Refactorings of " + projectName + " project can be corrupted.");
            }
        }
        printResults(detectedRefactorings);
    }

    private static void printResults(@NotNull List<RepositoryDetectedRefactorings> detectedRefactorings) {
        System.out.println("====================STATISTIC====================");
        for (RepositoryDetectedRefactorings repositoryDetectedRefactorings : detectedRefactorings) {
            System.out.println("--------------------------------");
            System.out.println("Project: " + ParsingUtils.getProjectName(repositoryDetectedRefactorings.getRepository()));
            System.out.println("Branch: " + repositoryDetectedRefactorings.getBranch());
            if (repositoryDetectedRefactorings.isSuccess()) {
                System.out.println("Result: success");
                System.out.println("Processed commits: " +
                        Objects.requireNonNull(repositoryDetectedRefactorings.getExecutionInfo()).getProcessedCommitsNumber());
                System.out.println("Processed commits with refactorings: " +
                        Objects.requireNonNull(repositoryDetectedRefactorings.getExecutionInfo()).getProcessedCommitsWithRefactoringsNumber());
                System.out.println("Median of number of refactorings in commits (only for commits that contain refactorings): " +
                        Objects.requireNonNull(repositoryDetectedRefactorings.getExecutionInfo()).getMedianOfNotNullRefactoringsNumbers());
                System.out.println("Detected: " +
                        Objects.requireNonNull(repositoryDetectedRefactorings.getDetectedRefactorings()).size());
            } else {
                System.out.println("Result: failed");
                System.out.println("Error type: " +
                        Objects.requireNonNull(repositoryDetectedRefactorings.getException()).getClass().getCanonicalName());
                System.out.println("Reason: " +
                        Objects.requireNonNull(repositoryDetectedRefactorings.getException()).getMessage());
            }
        }
        System.out.println("--------------------------------");
        System.out.println("=================================================");
    }

    private static void printUsage() {
        System.out.println("Usage: refactoring-detection <path to file with repositories list>" +
                " <path to directory where to save detected refactorings>" +
                " <detection tool (optional)>");
    }

    private static void exitWithError(@NotNull String errorDescription, @NotNull Throwable e) {
        LOGGER.error(errorDescription, e);
        System.err.println(errorDescription);
        printExceptionInformation(e);
        System.err.println("Application terminated.");
        exit(-1);
    }

    static void printExceptionInformation(Throwable e) {
        System.err.println("Error type: " + e.getClass().getCanonicalName());
        System.err.println("Reason: " + e.getMessage());
    }
}
