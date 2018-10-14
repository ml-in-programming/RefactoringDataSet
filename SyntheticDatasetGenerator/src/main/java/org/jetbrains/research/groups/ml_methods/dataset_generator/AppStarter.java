package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.ide.impl.PatchProjectUtil;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.MoveMembersRefactoring;
import com.intellij.refactoring.Refactoring;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodHandler;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodProcessor;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.exceptions.UsagesConflictsException;
import org.jetbrains.research.groups.ml_methods.dataset_generator.filters.GettersFilter;
import org.jetbrains.research.groups.ml_methods.dataset_generator.filters.StaticMethodsFilter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AppStarter implements ApplicationStarter {
    private String projectFolderPath = "";

    @Override
    public String getCommandName() {
        return "generate-dataset";
    }

    @Override
    public void premain(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Invalid number of arguments!");
            System.exit(1);
            return;
        }

        projectFolderPath = new File(args[1]).getAbsolutePath().replace(File.separatorChar, '/');
    }

    @Override
    public void main(String[] args) {
        ApplicationEx application = (ApplicationEx) ApplicationManager.getApplication();

        try {
            application.doNotSave();
            Project project = ProjectUtil.openOrImport(
                projectFolderPath,
                null,
                false
            );

            if (project == null) {
                System.out.println("Unable to open project: " + projectFolderPath);
                System.exit(1);
                return;
            }

            application.runWriteAction(() ->
                VirtualFileManager.getInstance()
                        .refreshWithoutFileWatcher(false)
            );

            PatchProjectUtil.patchProject(project);

            System.out.println("Project " + projectFolderPath + " is opened");
            doStuff(project);
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e);
            e.printStackTrace();
        }

        application.exit(true, true);
    }

    private void doStuff(final @NotNull Project project) throws UsagesConflictsException {
        Application application = ApplicationManager.getApplication();

        List<PsiJavaFile> javaFiles =
            application.runReadAction(
                (Computable<List<PsiJavaFile>>)
                    () -> ExtractingUtils.extractJavaFiles(project)
            );

        List<SmartPsiElementPointer<PsiClass>> classes =
            application.runReadAction(
                (Computable<List<SmartPsiElementPointer<PsiClass>>>)
                    () -> ExtractingUtils.extractClasses(javaFiles)
            );

        List<SmartPsiElementPointer<PsiMethod>> methods =
            application.runReadAction(
                (Computable<List<SmartPsiElementPointer<PsiMethod>>>)
                    () -> ExtractingUtils.extractMethods(javaFiles)
            );

        System.out.println("Total number of java files: " + javaFiles.size());
        System.out.println("Total number of classes: " + classes.size());
        System.out.println("Total number of method: " + methods.size());

        List<PsiMethod> filteredMethods = methods.stream()
            .map(SmartPsiElementPointer::getElement)
            // .filter(new StaticMethodsFilter())
            .filter(new GettersFilter())
            .collect(Collectors.toList());

        System.out.println("Number of methods after filtration: " + filteredMethods.size());

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
