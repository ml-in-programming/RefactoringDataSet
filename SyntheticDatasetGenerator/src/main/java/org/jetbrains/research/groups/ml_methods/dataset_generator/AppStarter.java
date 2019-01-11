package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.ide.impl.PatchProjectUtil;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodHandler;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodProcessor;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.exceptions.UsagesConflictsException;
import org.jetbrains.research.groups.ml_methods.dataset_generator.rewriter.MethodRewriter;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.exceptions.UnsupportedDirectoriesLayoutException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils.fullyQualifiedName;
import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.PreprocessingUtils.addAllPossibleSourceRoots;

public class AppStarter implements ApplicationStarter {
    private String projectFolderPath = "";

    private Path outputDir;

    private static final @NotNull Logger log = Logger.getLogger(AppStarter.class);

    @Override
    public String getCommandName() {
        return "generate-dataset";
    }

    @Override
    public void premain(String[] args) {
        if (args == null || args.length != 3) {
            System.err.println("Invalid number of arguments!");
            System.exit(1);
            return;
        }

        projectFolderPath = new File(args[1]).getAbsolutePath().replace(File.separatorChar, '/');

        Path tmp = Paths.get(projectFolderPath);
        outputDir = Paths.get(args[2]).resolve(tmp.getName(tmp.getNameCount() - 1));
    }

    @Override
    public void main(String[] args) {
        String logFileName = outputDir.resolve("log").toString();

        try {
            log.addAppender(new FileAppender(new PatternLayout("%d [%p] %m%n"), logFileName));
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + logFileName);
        }

        ApplicationEx application = (ApplicationEx) ApplicationManager.getApplication();

        try {
            application.doNotSave();
            Project project = ProjectUtil.openOrImport(
                projectFolderPath,
                null,
                false
            );

            if (project == null) {
                log.error("Unable to open project: " + projectFolderPath);
                System.exit(1);
                return;
            }

            application.runWriteAction(() ->
                VirtualFileManager.getInstance()
                        .refreshWithoutFileWatcher(false)
            );

            PatchProjectUtil.patchProject(project);

            log.info("Project " + projectFolderPath + " is opened");

            application.runWriteAction(() -> {
                try {
                    addAllPossibleSourceRoots(project);
                } catch (UnsupportedDirectoriesLayoutException e) {
                    throw new RuntimeException(e);
                }
            });

            doStuff(project, outputDir);
        } catch (Throwable e) {
            log.error("Exception occurred: " + e.getMessage() + " [" + e + "]");
            for (StackTraceElement element : e.getStackTrace()) {
                log.error(element);
            }
        }

        application.exit(true, true);
    }

    private void doStuff(final @NotNull Project project, final @NotNull Path outputDir) throws Exception {
        final Ref<Exception> exceptionRef = new Ref<>(null);
        ProjectInfo projectInfo = ApplicationManager.getApplication().runReadAction(
            (Computable<ProjectInfo>) () -> {
                ProjectInfo info = new ProjectInfo(project);

                log.info("Total number of java files: " + info.getAllJavaFiles().size());
                log.info("Total number of source java files: " + info.getSourceJavaFiles().size());
                log.info("Total number of classes: " + info.getClasses().size());
                log.info("Total number of methods: " + info.getMethods().size());
                log.info("Number of methods after filtration: " + info.getMethodsAfterFiltration().size());

                try {
                    new PathContextExtractor(info).extract(outputDir);
                } catch (Exception e) {
                    exceptionRef.set(e);
                }

                return info;
            }
        );

        if (!exceptionRef.isNull()) {
            throw exceptionRef.get();
        }

        /*MethodRewriter rewriter = new MethodRewriter(projectInfo);
        projectInfo.getMethodsAfterFiltration().forEach(it -> {
            // WriteCommandAction.runWriteCommandAction(project, () -> rewriter.rewrite(it));

            ApplicationManager.getApplication().runReadAction(() -> {
                System.out.println(fullyQualifiedName(it));
                System.out.println(it.getText());
                System.out.println('\n');
            });
        });*/

//        try (CsvWriter csvWriter = new CsvWriter("/home/ivan/out.csv")) {
//            FeaturesExtractor extractor = new FeaturesExtractor(csvWriter, projectInfo);
//
//            for (PsiMethod method : projectInfo.getMethodsAfterFiltration()) {
//                extractor.extractFeatures(method);
//            }
//        }

//        SmartPsiElementPointer<PsiMethod> method =
//            methods.stream().filter(it -> it.getElement().getName().equals("aMethod")).findAny().get();
//
//        Ref<UsagesConflictsException> usagesConflictsExceptionRef = new Ref<>();
//        DumbService.getInstance(project).runWhenSmart(
//            () -> application.runReadAction(() -> {
//                try {
//                    moveMethod(project, method, classes);
//                } catch (UsagesConflictsException e) {
//                    usagesConflictsExceptionRef.set(e);
//                }
//            })
//        );
//
//        if (!usagesConflictsExceptionRef.isNull()) {
//            throw usagesConflictsExceptionRef.get();
//        }
    }

    private void moveMethod(
        final @NotNull Project project,
        final @NotNull SmartPsiElementPointer<PsiMethod> method,
        final @NotNull List<SmartPsiElementPointer<PsiClass>> classes
    ) throws UsagesConflictsException {
        PsiMethod psiMethod = method.getElement();
        if (psiMethod == null) {
            return;
        }

        List<PsiClass> targets =
            classes.stream()
            .map(SmartPsiElementPointer::getElement)
            .filter(Objects::nonNull).filter(it -> !it.equals(psiMethod.getContainingClass()))
            .collect(Collectors.toList());

        String targetName = targets.get(0).getQualifiedName();
        System.out.println("Moving " + psiMethod.getName() + " to " + targetName);

        PsiVariable targetVariable = psiMethod.getParameterList().getParameters()[0];
        Map<PsiClass, String> parameterNames = MoveInstanceMethodHandler.suggestParameterNames(psiMethod, targetVariable);

        for (Map.Entry<PsiClass, String> entry : parameterNames.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }

        MoveInstanceMethodProcessor moveMethodProcessor =
            new MoveInstanceMethodProcessor(
                project,
                psiMethod,
                targetVariable,
                PsiModifier.PUBLIC,
                parameterNames
            );

        moveMethodProcessor.run();
    }
}
