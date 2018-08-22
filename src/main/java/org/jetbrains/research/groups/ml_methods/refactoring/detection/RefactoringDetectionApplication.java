package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RefactoringDetectionApplication {
    private static final String DEFAULT_DETECTION_TOOL_NAME = "RMiner";

    public static void main(@NotNull String[] args) {
        if (args.length != 2 && args.length != 1) {
            printUsage();
            return;
        }

        Path pathToRepositoriesFile = Paths.get(args[0]);
        String detectionToolName = args.length == 2 ? args[1] : DEFAULT_DETECTION_TOOL_NAME;
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
            System.err.println("Error occurred during parsing passed repository urls.");
            System.err.println("Reason: " + e.getMessage());
            System.err.println("Application terminated.");
            return;
        }
        List<MoveMethodRefactoringFromVCS> detectedRefactorings = refactoringDetectionTool.detect(repositories);
        System.out.println("Detected " + detectedRefactorings.size() + " move method refactorings");
        detectedRefactorings.forEach(System.out::println);
    }

    private static void printUsage() {
        System.out.println("Usage: refactoring-detection <path to file with repositories list> <detection tool (optional)>");
    }
}
