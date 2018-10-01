package org.jetbrains.research.groups.ml_methods.refactorings_analyzer;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.CommitDetectionSuccess;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionSuccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RefactoringsAnalyzerApplication {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("One argument is required: data folder path");
            return;
        }

        try {
            new RefactoringsAnalyzerApplication().run(args[0]);
        } catch (Throwable e) {
            System.err.printf("Unexpected exception %s: %s\n", e, e.getMessage());
            e.printStackTrace();
        }
    }

    private void run(final @NotNull String dataFolderPath) throws FileNotFoundException {
        File dataFolder = new File(dataFolderPath);
        File[] repositoryFiles = dataFolder.listFiles();

        if (repositoryFiles == null) {
            System.out.printf("Given data folder does not exist");
            return;
        }

        final int commitsToSample = 5;

        List<RepositoryReport> reportsList = new ArrayList<>();
        for (File repositoryFile : repositoryFiles) {
            if (!repositoryFile.isFile()) {
                System.out.println("Unexpected entity: " + repositoryFile.getName());
                continue;
            }

            RepositoryReport report = analyzeRepository(
                new Gson().fromJson(
                    new FileReader(repositoryFile),
                    RepositoryDetectionSuccess.class
                ),
                commitsToSample
            );

            reportsList.add(report);
            System.out.printf("%s\n", report);
        }

        AggregatedReport summary = new AggregatedReport(reportsList);
        System.out.printf("Summary\n%s\n", summary);
    }

    private @NotNull RepositoryReport analyzeRepository(
        final @NotNull RepositoryDetectionSuccess repositoryInfo,
        final int commitsToSample
    ) {

        int failedCommitsNum = repositoryInfo.getCommitDetectionFailures().size();
        int processedCommitsNum =
            repositoryInfo.getCommitDetectionSuccesses().size() + failedCommitsNum;

        List<CommitDetectionSuccess> commitsWithRefactorings =
            repositoryInfo.getCommitDetectionSuccesses()
                .stream()
                .filter(commitInfo -> !commitInfo.getRefactorings().isEmpty())
                .collect(Collectors.toList());

        int commitsWithMoveMethodRefactorings = commitsWithRefactorings.size();

        int detectedRefactoringsNum = (int) commitsWithRefactorings.stream()
            .mapToLong(commitInfo -> commitInfo.getRefactorings().size()).sum();

        Predicate<MoveMethodRefactoring> allFilters =
            new ConstructorFilter()
            .and(new ObjectMethodsFilter())
            .and(new TestsFilter())
            .and(new NewTargetClassFilter())
            .and(new RemovedOldClassFilter())
            .and(new GettersFilter())
            .and(new StaticMethodsFilter());

        commitsWithRefactorings =
            commitsWithRefactorings.stream()
                .map(commitInfo -> new CommitDetectionSuccess(
                    commitInfo.getCommitHash(),
                    commitInfo.getRefactorings()
                        .stream()
                        .filter(allFilters)
                        .collect(Collectors.toList()))
                ).filter(commitInfo -> !commitInfo.getRefactorings().isEmpty())
                .collect(Collectors.toList());

        int filteredCommitsNum = commitsWithRefactorings.size();

        int filteredRefactoringsNum = (int) commitsWithRefactorings.stream()
            .mapToLong(commitInfo -> commitInfo.getRefactorings().size()).sum();

        Collections.shuffle(commitsWithRefactorings);

        return new RepositoryReport(
            repositoryInfo.getRepository().toString(),
            processedCommitsNum,
            failedCommitsNum,
            commitsWithMoveMethodRefactorings,
            detectedRefactoringsNum,
            filteredCommitsNum,
            filteredRefactoringsNum,
            commitsWithRefactorings.stream().limit(commitsToSample).collect(Collectors.toList())
        );
    }

    private static class ConstructorFilter implements Predicate<MoveMethodRefactoring> {
        @Override
        public boolean test(final @NotNull MoveMethodRefactoring moveMethodRefactoring) {
            return test(moveMethodRefactoring.getOriginalMethodInfo()) &&
                   test(moveMethodRefactoring.getMovedMethodInfo());
        }

        private boolean test(
            final @NotNull MoveMethodRefactoring.MethodRefactoringInfo methodInfo
        ) {
            return !getClassSimpleName(methodInfo.getClassQualifiedName()).equals(
                methodInfo.getMethodName()
            );
        }

        private @NotNull String getClassSimpleName(final @NotNull String classQualifiedName) {
            return classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);
        }
    }

    private static class ObjectMethodsFilter implements Predicate<MoveMethodRefactoring> {
        @Override
        public boolean test(final @NotNull MoveMethodRefactoring moveMethodRefactoring) {
            return test(moveMethodRefactoring.getOriginalMethodInfo()) &&
                    test(moveMethodRefactoring.getMovedMethodInfo());
        }

        private boolean test(
            final @NotNull MoveMethodRefactoring.MethodRefactoringInfo methodInfo
        ) {
            return !isEquals(methodInfo) && !isHashCode(methodInfo);
        }

        private boolean isEquals(
            final @NotNull MoveMethodRefactoring.MethodRefactoringInfo methodInfo
        ) {
            List<String> paramNames = methodInfo.getParamsClassesSimpleNames();

            return methodInfo.getMethodName().equals("equals") &&
                "boolean".equals(methodInfo.getReturnType()) &&
                paramNames.size() == 1 &&
                paramNames.get(0).equals("Object");
        }

        private boolean isHashCode(
            final @NotNull MoveMethodRefactoring.MethodRefactoringInfo methodInfo
        ) {
            List<String> paramNames = methodInfo.getParamsClassesSimpleNames();

            return methodInfo.getMethodName().equals("hashCode") &&
                "int".equals(methodInfo.getReturnType()) &&
                paramNames.isEmpty();
        }
    }

    private static class TestsFilter implements Predicate<MoveMethodRefactoring> {
        @Override
        public boolean test(final @NotNull MoveMethodRefactoring moveMethodRefactoring) {
            return test(moveMethodRefactoring.getOriginalMethodInfo()) &&
                test(moveMethodRefactoring.getMovedMethodInfo());
        }

        private boolean test(
            final @NotNull MoveMethodRefactoring.MethodRefactoringInfo methodInfo
        ) {
            String className = methodInfo.getClassQualifiedName().toLowerCase();
            return !className.endsWith("test") && !className.endsWith("tests");
        }
    }

    private static class NewTargetClassFilter implements Predicate<MoveMethodRefactoring> {
        @Override
        public boolean test(final @NotNull MoveMethodRefactoring moveMethodRefactoring) {
            return moveMethodRefactoring.getMovedMethodInfo()
                .getRefactoringFilePaths()
                .getFilePathBefore() != null;
        }
    }

    private static class RemovedOldClassFilter implements Predicate<MoveMethodRefactoring> {
        @Override
        public boolean test(final @NotNull MoveMethodRefactoring moveMethodRefactoring) {
            return moveMethodRefactoring.getOriginalMethodInfo()
                    .getRefactoringFilePaths()
                    .getFilePathAfter() != null;
        }
    }

    private static class GettersFilter implements Predicate<MoveMethodRefactoring> {
        @Override
        public boolean test(final @NotNull MoveMethodRefactoring moveMethodRefactoring) {
            return test(moveMethodRefactoring.getOriginalMethodInfo()) &&
                test(moveMethodRefactoring.getMovedMethodInfo());
        }

        private boolean test(
            final @NotNull MoveMethodRefactoring.MethodRefactoringInfo methodInfo
        ) {
            return "void".equals(methodInfo.getReturnType()) ||
                !methodInfo.getParamsClassesSimpleNames().isEmpty() ||
                methodInfo.getOriginalStatementsCount() > 1;
        }
    }

    private static class StaticMethodsFilter implements Predicate<MoveMethodRefactoring> {
        @Override
        public boolean test(final @NotNull MoveMethodRefactoring moveMethodRefactoring) {
            return test(moveMethodRefactoring.getOriginalMethodInfo()) &&
                    test(moveMethodRefactoring.getMovedMethodInfo());
        }

        private boolean test(
            final @NotNull MoveMethodRefactoring.MethodRefactoringInfo methodInfo
        ) {
            return !methodInfo.isStatic();
        }
    }
}
