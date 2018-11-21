package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.ide.impl.PatchProjectUtil;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodHandler;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.exceptions.UsagesConflictsException;
import org.jetbrains.research.groups.ml_methods.dataset_generator.filters.classes.*;
import org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods.*;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.ExtractingUtils;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.exceptions.UnsupportedDirectoriesLayoutException;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils.fullyQualifiedName;
import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils.whoseGetter;
import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils.whoseSetter;
import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.PreprocessingUtils.addAllPossibleSourceRoots;

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

            application.runWriteAction(() -> {
                try {
                    addAllPossibleSourceRoots(project);
                } catch (UnsupportedDirectoriesLayoutException e) {
                    throw new RuntimeException(e);
                }
            });

            application.runReadAction(() -> doStuff(project));
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e);
            e.printStackTrace();
        }

        application.exit(true, true);
    }

    private void doStuff(final @NotNull Project project) {
        List<PsiJavaFile> allJavaFiles = ExtractingUtils.extractAllJavaFiles(project);
        List<PsiJavaFile> sourceJavaFiles = ExtractingUtils.extractSourceJavaFiles(project);

        List<PsiClass> classes = ExtractingUtils.extractClasses(allJavaFiles)
            .stream()
            .filter(new TypeParametersFilter())
            .filter(new InterfacesFilter())
            .filter(new AnnotationTypesFilter())
            .filter(new TestsFilter())
            .filter(new BuildersFilter())
            .collect(Collectors.toList());

        List<PsiMethod> methods = ExtractingUtils.extractMethods(classes);

        Set<PsiClass> allInterestingClasses = new HashSet<>(classes);

        System.out.println("Total number of java files: " + allJavaFiles.size());
        System.out.println("Total number of source java files: " + sourceJavaFiles.size());
        System.out.println("Total number of classes: " + classes.size());
        System.out.println("Total number of method: " + methods.size());

        Set<PsiField> fieldsWithGetter = new HashSet<>();
        Set<PsiField> fieldsWithSetter = new HashSet<>();

        methods.forEach(it -> {
            if (!it.hasModifierProperty(PsiModifier.PUBLIC)) {
                return;
            }

            whoseGetter(it).ifPresent(fieldsWithGetter::add);
            whoseSetter(it).ifPresent(fieldsWithSetter::add);
        });

        List<PsiMethod> filteredMethods =
            methods.stream()
                .filter(new ConstructorsFilter())
                .filter(new AbstractMethodsFilter())
                .filter(new StaticMethodsFilter())
                .filter(new GettersFilter())
                .filter(new SettersFilter())
                .filter(new NoTargetsMethodsFilter(allInterestingClasses))
                .filter(new OverridingMethodsFilter())
                .filter(new OverriddenMethodsFilter())
                .filter(new PrivateMethodsCallersFilter())
                .filter(new PrivateFieldAccessorsFilter(fieldsWithGetter, fieldsWithSetter))
                .filter(new EmptyMethodsFilter())
                .filter(new ExceptionsThrowersFilter())
                .filter(new SimpleDelegationsFilter())
                .collect(Collectors.toList());

        System.out.println("Number of methods after filtration: " + filteredMethods.size());

        filteredMethods.forEach(it -> {
            System.out.println(fullyQualifiedName(it));
            System.out.println(it.getText());
            System.out.println('\n');
        });

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
