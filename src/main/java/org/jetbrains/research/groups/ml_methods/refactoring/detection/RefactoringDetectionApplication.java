package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoriesDetectionResults;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionResult;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionResultCombiner;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.tools.RMiner;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.tools.RefactoringDetectionTool;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.ErrorReporter;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.RepositoriesReader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.System.exit;
import static org.refactoringminer.api.RefactoringType.MOVE_OPERATION;

public class RefactoringDetectionApplication {
    @NotNull
    private static final String DEFAULT_DETECTION_TOOL_NAME = "RMiner";

    public static void main(@NotNull String[] args) {
        try {
            RefactoringDetectionApplication application = new RefactoringDetectionApplication();
            application.run(parseArguments(args));
        } catch (Throwable e) {
            String errorMessage = "Unexpected exception occurred";
            ErrorReporter.exitWithError(errorMessage, e, RefactoringDetectionApplication.class);
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
                refactoringDetectionTool = new RMiner(MOVE_OPERATION);
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
            String errorMessage = "Error occurred during parsing passed repository urls.";
            ErrorReporter.exitWithError(errorMessage, e, this.getClass());
            return;
        }
        List<RepositoryDetectionResult> detectedRefactorings;
        try {
            detectedRefactorings = argumentsHolder.refactoringDetectionTool
                    .detectAndSave(repositories, argumentsHolder.outputDirPath);
        } catch (Exception e) {
            String errorMessage = "Error occurred during refactoring detection.";
            ErrorReporter.exitWithError(errorMessage, e, this.getClass());
            return;
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
}
