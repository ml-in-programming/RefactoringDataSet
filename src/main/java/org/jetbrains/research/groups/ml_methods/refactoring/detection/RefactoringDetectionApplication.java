package org.jetbrains.research.groups.ml_methods.refactoring.detection;

public class RefactoringDetectionApplication {
    public static void main(String[] args) {
        printUsage();
    }

    private static void printUsage() {
        System.out.println("refactoring-detection <path to file with repositories list> <detection tool (optional)>");
    }
}
