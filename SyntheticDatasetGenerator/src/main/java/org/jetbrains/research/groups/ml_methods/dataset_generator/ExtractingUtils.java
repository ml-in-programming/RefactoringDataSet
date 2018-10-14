package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExtractingUtils {
    private ExtractingUtils() { }

    public static @NotNull List<PsiJavaFile> extractJavaFiles(final @NotNull Project project) {
        List<PsiJavaFile> javaFiles = new ArrayList<>();

        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(
            virtualFile -> {
                if (!virtualFile.isDirectory() && "java".equals(virtualFile.getExtension())) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

                    if (psiFile == null) {
                        System.err.println("Invalid virtual file encountered!");
                        return true;
                    }

                    if (!(psiFile instanceof PsiJavaFile)) {
                        System.err.println("Not a java file with java extension encountered!");
                        return true;
                    }

                    javaFiles.add((PsiJavaFile) psiFile);
                }

                return true;
            }
        );

        return javaFiles;
    }

    public static @NotNull List<PsiClass> extractClasses(
        final @NotNull PsiJavaFile javaFile
    ) {
        List<PsiClass> classes = new ArrayList<>();

        new JavaRecursiveElementVisitor() {
            @Override
            public void visitClass(final @NotNull PsiClass aClass) {
                super.visitClass(aClass);

                if (!aClass.isInterface() && !aClass.isAnnotationType()) {
                    classes.add(aClass);
                }
            }
        }.visitElement(javaFile);

        return classes;
    }

    public static @NotNull List<PsiClass> extractClasses(
        final @NotNull List<PsiJavaFile> javaFiles
    ) {
        return javaFiles.stream()
            .flatMap(file -> extractClasses(file).stream())
            .collect(Collectors.toList());
    }

    public static @NotNull List<PsiMethod> extractMethods(
        final @NotNull PsiJavaFile javaFile
    ) {
        List<PsiMethod> methods = new ArrayList<>();

        new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(final @NotNull PsiMethod method) {
                super.visitMethod(method);

                methods.add(method);
            }
        }.visitElement(javaFile);

        return methods;
    }

    public static @NotNull List<PsiMethod> extractMethods(
        final @NotNull List<PsiJavaFile> javaFiles
    ) {
        return javaFiles.stream()
            .flatMap(file -> extractMethods(file).stream())
            .collect(Collectors.toList());
    }
}
