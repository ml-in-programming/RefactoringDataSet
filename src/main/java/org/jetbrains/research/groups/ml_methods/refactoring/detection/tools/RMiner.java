package org.jetbrains.research.groups.ml_methods.refactoring.detection.tools;

import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.MoveMethodCommitRefactorings;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.MoveMethodRefactoring.RefactoringFilePaths;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RefactoringDetectionExecutionInfo;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.results.RepositoryDetectionSuccess;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.ErrorReporter;
import org.jetbrains.research.groups.ml_methods.refactoring.detection.utils.ParsingUtils;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.refactoringminer.api.RefactoringType.*;

class RMiner extends DefaultBranchesDetectionTool {
    @NotNull
    private static final Logger LOGGER = Logger.getLogger(RMiner.class);
    @NotNull
    private static final RefactoringType[] interestingRefactoringsTypes = {
            MOVE_OPERATION,
            PULL_UP_OPERATION,
            PUSH_DOWN_OPERATION
    };

    @NotNull
    private static FilePathsForRefactoringMapper
    findRefactoringsFilePaths(@NotNull List<MoveOperationRefactoring> refactorings,
                              GitService gitService,
                              Repository repository,
                              RevCommit commitData) {
        return new FilePathsForRefactoringMapper(
                findOriginalFilePathsBefore(refactorings, gitService, repository, commitData.getParent(0)),
                findMovedFilePathsBefore(refactorings, gitService, repository, commitData.getParent(0)),
                findOriginalFilePathsAfter(refactorings, gitService, repository, commitData),
                findMovedFilePathsAfter(refactorings, gitService, repository, commitData)
        );
    }

    @NotNull
    private static Map<Refactoring, Path>
    findMovedFilePathsAfter(@NotNull List<MoveOperationRefactoring> refactorings,
                            GitService gitService,
                            Repository repository,
                            ObjectId commitId) {
        LOGGER.info("Searching for refactorings file paths to original classes after applying changes of this commit");
        return mapRefactoringsToFiles(refactorings,
                gitService,
                repository,
                commitId,
                refactoring -> Paths.get(refactoring.getMovedOperation().getLocationInfo().getFilePath()));
    }

    @NotNull
    private static Map<Refactoring, Optional<Path>>
    findOriginalFilePathsAfter(@NotNull List<MoveOperationRefactoring> refactorings,
                               GitService gitService,
                               Repository repository,
                               ObjectId commitId) {
        LOGGER.info("Searching for refactorings file paths to original classes after applying changes of this commit");
        return mapRefactoringsToFilesByWalking(refactorings, gitService, repository, commitId, true);
    }

    @NotNull
    private static Map<Refactoring, Optional<Path>>
    findMovedFilePathsBefore(@NotNull List<MoveOperationRefactoring> refactorings,
                             GitService gitService,
                             Repository repository,
                             ObjectId commitId) {
        LOGGER.info("Searching for refactorings file paths to moved classes before applying changes of this commit");
        return mapRefactoringsToFilesByWalking(refactorings, gitService, repository, commitId, false);
    }

    @NotNull
    private static Map<Refactoring, Path>
    findOriginalFilePathsBefore(@NotNull List<MoveOperationRefactoring> refactorings,
                                @NotNull GitService gitService,
                                @NotNull Repository repository,
                                @NotNull ObjectId commitId) {
        LOGGER.info("Searching for refactorings file paths to original classes before applying changes of this commit");
        return mapRefactoringsToFiles(refactorings,
                gitService,
                repository,
                commitId,
                refactoring -> Paths.get(refactoring.getOriginalOperation().getLocationInfo().getFilePath()));
    }

    @NotNull
    private static Map<Refactoring, Optional<Path>>
    mapRefactoringsToFilesByWalking(@NotNull List<MoveOperationRefactoring> refactorings,
                                    @NotNull GitService gitService,
                                    @NotNull Repository repository,
                                    @NotNull ObjectId commitId,
                                    boolean isOriginal) {
        return mapRefactoringsToFiles(refactorings,
                gitService,
                repository,
                commitId,
                refactoring -> {
                    try {
                        return Files.walk(repository.getWorkTree().toPath())
                                .filter(path ->
                                        path.endsWith(Paths.get(isOriginal ?
                                                ParsingUtils.getPathFromClassQualifiedName(
                                                        refactoring.getOriginalOperation().getClassName(),
                                                        refactoring.getOriginalOperation().getLocationInfo()
                                                                .getFilePath()) :
                                                ParsingUtils.getPathFromClassQualifiedName(
                                                        refactoring.getMovedOperation().getClassName(),
                                                        refactoring.getMovedOperation().getLocationInfo()
                                                                .getFilePath()
                                                ))))
                                .findAny();
                    } catch (IOException e) {
                        String errorMessage = "Error occurred during walking from repository work tree in search" +
                                " for refactorings file paths to " +
                                (isOriginal ? "original" : "moved") +
                                " classes in current commit";
                        ErrorReporter.reportError(errorMessage, e, RMiner.class);
                    }
                    return Optional.empty();
                });
    }

    @NotNull
    private static <T> Map<Refactoring, T>
    mapRefactoringsToFiles(@NotNull List<MoveOperationRefactoring> refactorings,
                           @NotNull GitService gitService,
                           @NotNull Repository repository,
                           @NotNull ObjectId commitId,
                           @NotNull Function<MoveOperationRefactoring, T> mappingFunction) {
        try {
            gitService.checkout(repository, commitId.getName());
        } catch (Exception e) {
            String errorMessage = "Error occurred during checking out " +
                    repository + " repository on commit: " + commitId.getName();
            ErrorReporter.reportError(errorMessage, e, RMiner.class);
        }
        return refactorings.stream().collect(Collectors.toMap(Function.identity(), mappingFunction));
    }

    @NotNull
    @Override
    public RepositoryDetectionSuccess detect(@NotNull URL repositoryUrl, @NotNull String branch) throws Exception {
        String projectName = ParsingUtils.getProjectName(repositoryUrl);
        LOGGER.info("Started detection for " + projectName + " project");
        System.out.println("Started detection for " + projectName + " project");
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMinerImpl miner = new GitHistoryRefactoringMinerImpl();
        miner.setRefactoringTypesToConsider(interestingRefactoringsTypes);

        String tmpDir = System.getProperty("java.io.tmpdir");
        Path downloadRepositoryPath = Paths.get(tmpDir, projectName);
        Repository repository = gitService.cloneIfNotExists(
                downloadRepositoryPath.toAbsolutePath().toString(),
                repositoryUrl.toString());

        List<MoveMethodCommitRefactorings> detectedRefactorings = new ArrayList<>();
        int commitsNumber = gitService.countCommits(repository, branch) - 1;
        List<Integer> refactoringsNumbersInCommit = new ArrayList<>();
        miner.detectAll(repository, branch, new RefactoringHandler() {
            private int processedCommitsNumber = 0;

            @Override
            public void handleException(String commitId, Exception e) {
                super.handleException(commitId, e);
                String errorMessage = "Error occurred during refactoring detection in " +
                        commitId + " commmit for repository" + repositoryUrl;
                ErrorReporter.reportError(errorMessage, e, this.getClass());
                processedCommitsNumber++;
            }

            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                LOGGER.info("Processed new commit. Commit hash: " + commitData.getId().getName());
                List<MoveOperationRefactoring> moveOperationRefactorings =
                        Collections.unmodifiableList(refactorings.stream()
                                .map(refactoring -> (MoveOperationRefactoring) refactoring)
                                .collect(Collectors.toList()));
                FilePathsForRefactoringMapper filePathsForRefactoringMapper =
                        findRefactoringsFilePaths(moveOperationRefactorings, gitService, repository, commitData);
                List<MoveMethodRefactoring> commitRefactorings = new ArrayList<>();
                for (MoveOperationRefactoring moveOperationRefactoring : moveOperationRefactorings) {
                    LOGGER.info("Refactoring: " + moveOperationRefactoring.toString());

                    String targetClassQualifiedName = moveOperationRefactoring.getMovedOperation().getClassName();
                    String originalClassQualifiedName = moveOperationRefactoring.getOriginalOperation().getClassName();
                    String movedMethodName = moveOperationRefactoring.getMovedOperation().getName();
                    String originalMethodName = moveOperationRefactoring.getOriginalOperation().getName();
                    List<String> movedParamsClassesQualifiedNames =
                            moveOperationRefactoring.getMovedOperation().getParametersWithoutReturnType().stream()
                                    .map(umlParameter -> umlParameter.getType().toString())
                                    .collect(Collectors.toList());
                    List<String> originalParamsClassesQualifiedNames =
                            moveOperationRefactoring.getOriginalOperation().getParametersWithoutReturnType().stream()
                                    .map(umlParameter -> umlParameter.getType().toString())
                                    .collect(Collectors.toList());
                    RefactoringFilePaths refactoringFilePaths =
                            new RefactoringFilePaths(
                                    filePathsForRefactoringMapper.getOriginalBefore(moveOperationRefactoring),
                                    filePathsForRefactoringMapper.getMovedBefore(moveOperationRefactoring).isPresent() ?
                                            Paths.get(tmpDir, projectName)
                                                    .relativize(filePathsForRefactoringMapper
                                                            .getMovedBefore(moveOperationRefactoring).get()) :
                                            null,
                                    filePathsForRefactoringMapper.getOriginalAfter(moveOperationRefactoring).isPresent() ?
                                            Paths.get(tmpDir, projectName)
                                                    .relativize(filePathsForRefactoringMapper
                                                            .getOriginalAfter(moveOperationRefactoring).get()) :
                                            null,
                                    filePathsForRefactoringMapper.getMovedAfter(moveOperationRefactoring)
                            );
                    commitRefactorings.add(
                            new MoveMethodRefactoring(
                                    targetClassQualifiedName,
                                    originalClassQualifiedName,
                                    originalMethodName,
                                    movedMethodName,
                                    originalParamsClassesQualifiedNames,
                                    movedParamsClassesQualifiedNames,
                                    refactoringFilePaths
                            )
                    );
                }
                if (moveOperationRefactorings.size() != 0) {
                    refactoringsNumbersInCommit.add(moveOperationRefactorings.size());
                    detectedRefactorings.add(
                            new MoveMethodCommitRefactorings(
                                    commitData.getId().getName(),
                                    commitRefactorings
                            )
                    );
                }
                processedCommitsNumber++;
                System.out.println("Processed commits: " + processedCommitsNumber + " / " + commitsNumber);
            }
        });
        RefactoringDetectionExecutionInfo executionInfo =
                new RefactoringDetectionExecutionInfo(
                        commitsNumber,
                        refactoringsNumbersInCommit
                );
        FileUtils.deleteDirectory(downloadRepositoryPath.toFile());
        return new RepositoryDetectionSuccess(repositoryUrl, branch, executionInfo, detectedRefactorings);
    }

    private static class FilePathsForRefactoringMapper {
        @NotNull
        private final Map<Refactoring, Path> originalFilePathsBefore;
        @NotNull
        private final Map<Refactoring, Optional<Path>> movedFilePathsBefore;
        @NotNull
        private final Map<Refactoring, Optional<Path>> originalFilePathsAfter;
        @NotNull
        private final Map<Refactoring, Path> movedFilePathsAfter;

        private FilePathsForRefactoringMapper(@NotNull Map<Refactoring, Path> originalFilePathsBefore,
                                              @NotNull Map<Refactoring, Optional<Path>> movedFilePathsBefore,
                                              @NotNull Map<Refactoring, Optional<Path>> originalFilePathsAfter,
                                              @NotNull Map<Refactoring, Path> movedFilePathsAfter) {
            this.originalFilePathsBefore = originalFilePathsBefore;
            this.movedFilePathsBefore = movedFilePathsBefore;
            this.originalFilePathsAfter = originalFilePathsAfter;
            this.movedFilePathsAfter = movedFilePathsAfter;
        }

        @NotNull
        private Path getOriginalBefore(@NotNull Refactoring refactoring) {
            return originalFilePathsBefore.get(refactoring);
        }

        @NotNull
        private Optional<Path> getMovedBefore(@NotNull Refactoring refactoring) {
            return movedFilePathsBefore.get(refactoring);
        }

        @NotNull
        private Optional<Path> getOriginalAfter(@NotNull Refactoring refactoring) {
            return originalFilePathsAfter.get(refactoring);
        }

        @NotNull
        private Path getMovedAfter(@NotNull Refactoring refactoring) {
            return movedFilePathsAfter.get(refactoring);
        }
    }
}
