package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import gr.uom.java.xmi.diff.MoveOperationRefactoring;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.Logging;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.refactoringminer.api.RefactoringType.MOVE_OPERATION;

class RMiner extends AbstractDetectionTool {
    private static final Logger LOGGER = Logging.getLogger(RMiner.class);
    private static final RefactoringType[] interestingRefactoringsTypes = {
            MOVE_OPERATION,
            RefactoringType.PULL_UP_OPERATION,
            RefactoringType.PUSH_DOWN_OPERATION
    };

    @NotNull
    @Override
    public DetectedRefactoringsInRepository detect(@NotNull URL repositoryUrl, @NotNull String branch) throws Exception {
        String projectName = ParsingUtils.getProjectName(repositoryUrl);
        LOGGER.info("Started detection for " + projectName + " project");
        System.out.println("Started detection for " + projectName + " project");
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMinerImpl miner = new GitHistoryRefactoringMinerImpl();
        // TODO: decide if we want to add RefactoringType.EXTRACT_AND_MOVE_OPERATION
        miner.setRefactoringTypesToConsider(interestingRefactoringsTypes);

        String tmpDir = System.getProperty("java.io.tmpdir");
        Path downloadRepositoryPath = Paths.get(tmpDir, projectName);
        Repository repository = gitService.cloneIfNotExists(
                downloadRepositoryPath.toAbsolutePath().toString(),
                repositoryUrl.toString());
        List<MoveMethodRefactoringFromVCS> detectedRefactorings = new ArrayList<>();
        int commitsNumber = gitService.countCommits(repository, branch);
        final AtomicInteger processedCommitsNumber = new AtomicInteger(0);
        miner.detectAll(repository, branch, new RefactoringHandler() {
            @Override
            public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                System.out.println("Processed commits: " + processedCommitsNumber.getAndIncrement() + " / " + commitsNumber);
                LOGGER.info("Processing new commit. Commit hash: " + commitData.getId().getName());
                for (Refactoring refactoring : refactorings) {
                    LOGGER.info("Refactoring: " + refactoring.toString());
                    MoveOperationRefactoring moveOperationRefactoring = (MoveOperationRefactoring) refactoring;
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
                    detectedRefactorings.add(
                            new MoveMethodRefactoringFromVCS(
                                    repositoryUrl,
                                    commitData.getId().getName(),
                                    targetClassQualifiedName,
                                    originalClassQualifiedName,
                                    originalMethodName,
                                    movedMethodName,
                                    originalParamsClassesQualifiedNames,
                                    movedParamsClassesQualifiedNames
                            )
                    );
                }
            }
        });
        return new DetectedRefactoringsInRepository(repositoryUrl, branch, detectedRefactorings);
    }
}
