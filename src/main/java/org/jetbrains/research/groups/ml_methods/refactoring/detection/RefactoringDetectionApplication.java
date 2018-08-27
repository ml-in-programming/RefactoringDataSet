package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoriesDetectionResults;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionResult;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionResultCombiner;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionSuccess;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.tools.RefactoringDetectionTool;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.tools.RefactoringDetectionToolFactory;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.ParsingUtils;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.RepositoriesReader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.System.exit;

public class RefactoringDetectionApplication {
    @NotNull
    private static final String DEFAULT_DETECTION_TOOL_NAME = "RMiner";
    @NotNull
    private static final Logger LOGGER = Logger.getLogger(RefactoringDetectionApplication.class);

    public static void main(@NotNull String[] args) {
        try {
            RefactoringDetectionApplication application = new RefactoringDetectionApplication();
            application.run(parseArguments(args));
        } catch (Throwable e) {
            exitWithError("Unexpected exception occurred", e);
        }
    }

    private static RefactoringDetectionApplicationArgumentsHolder parseArguments(@NotNull String[] args) {
        if (args.length != 3 && args.length != 2) {
            printUsage();
            exit(-1);
        }

        Path pathToRepositoriesFile = Paths.get(args[0]);
        Path outputDirPath = Paths.get(args[1]);
        String detectionToolName = args.length == 3 ? args[2] : DEFAULT_DETECTION_TOOL_NAME;
        RefactoringDetectionTool refactoringDetectionTool = null;
        switch (detectionToolName) {
            case "RMiner":
                refactoringDetectionTool = RefactoringDetectionToolFactory.createRMiner();
                break;
            default:
                System.out.println("No such tool. Try again.");
                printUsage();
                exit(-1);
        }
        return new RefactoringDetectionApplicationArgumentsHolder(pathToRepositoriesFile,
                outputDirPath, refactoringDetectionTool);
    }

    private void run(RefactoringDetectionApplicationArgumentsHolder argumentsHolder) {
        List<URL> repositories;
        try {
            repositories = RepositoriesReader.read(argumentsHolder.pathToRepositoriesFile);
        } catch (IOException e) {
            String errorDescription = "Error occurred during parsing passed repository urls.";
            exitWithError(errorDescription, e);
            return;
        }
        List<RepositoryDetectionResult> detectedRefactorings;
        try {
            detectedRefactorings = argumentsHolder.refactoringDetectionTool.detect(repositories);
        } catch (Exception e) {
            String errorDescription = "Error occurred during refactoring detection.";
            exitWithError(errorDescription, e);
            return;
        }
        for (RepositoryDetectionResult repositoryDetectionResult : detectedRefactorings) {
            if (repositoryDetectionResult instanceof RepositoryDetectionSuccess) {
                String projectName = ParsingUtils.getProjectName(repositoryDetectionResult.getRepository());
                Path outputFilePath = argumentsHolder.outputDirPath.resolve(projectName);
                try {
                    repositoryDetectionResult.write(outputFilePath);
                } catch (IOException e) {
                    System.err.println("Error occurred during writing to " + outputFilePath + " file.");
                    printExceptionInformation(e);
                    System.err.println("Refactorings of " + projectName + " project can be corrupted.");
                }
            }
        }
        printResults(detectedRefactorings);
    }

    private static class RefactoringDetectionApplicationArgumentsHolder {
        private final Path pathToRepositoriesFile;
        private final Path outputDirPath;
        private final RefactoringDetectionTool refactoringDetectionTool;

        private RefactoringDetectionApplicationArgumentsHolder(
                @NotNull Path pathToRepositoriesFile,
                @NotNull Path outputDirPath,
                @NotNull RefactoringDetectionTool refactoringDetectionTool) {
            this.pathToRepositoriesFile = pathToRepositoriesFile;
            this.outputDirPath = outputDirPath;
            this.refactoringDetectionTool = refactoringDetectionTool;
        }
    }

    private static void printResults(@NotNull List<RepositoryDetectionResult> detectedRefactorings) {
        System.out.println("====================STATISTICS====================");
        RepositoryDetectionResultCombiner repositoryDetectionResultCombiner =
                new RepositoryDetectionResultCombiner();
        for (RepositoryDetectionResult repositoryDetectionResult : detectedRefactorings) {
            repositoryDetectionResultCombiner.add(repositoryDetectionResult);
            System.out.println("--------------------------------");
            System.out.println(repositoryDetectionResult);
        }
        System.out.println("--------------------------------");
        RepositoriesDetectionResults totalRepositoriesResults = repositoryDetectionResultCombiner.combine();
        System.out.println("RESULTS FOR ALL REPOSITORIES");
        System.out.println(totalRepositoriesResults);
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

    public static void printExceptionInformation(Throwable e) {
        System.err.println("Error type: " + e.getClass().getCanonicalName());
        System.err.println("Reason: " + e.getMessage());
    }
}
