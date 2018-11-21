package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.filters.classes.*;
import org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods.*;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.ExtractingUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils.whoseGetter;
import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils.whoseSetter;

public class ProjectInfo {
    private final @NotNull List<PsiJavaFile> allJavaFiles;

    private final @NotNull List<PsiJavaFile> sourceJavaFiles;

    private final @NotNull List<PsiClass> classes;

    private final @NotNull List<PsiMethod> methods;

    private final @NotNull List<PsiMethod> filteredMethods;

    private final @NotNull Set<PsiClass> allInterestingClasses;

    public ProjectInfo(final @NotNull Project project) {
        allJavaFiles = ExtractingUtils.extractAllJavaFiles(project);
        sourceJavaFiles = ExtractingUtils.extractSourceJavaFiles(project);

        classes = ExtractingUtils.extractClasses(sourceJavaFiles)
                .stream()
                .filter(new TypeParametersFilter())
                .filter(new InterfacesFilter())
                .filter(new AnnotationTypesFilter())
                .filter(new TestsFilter())
                .filter(new BuildersFilter())
                .collect(Collectors.toList());

        methods = ExtractingUtils.extractMethods(classes);

        allInterestingClasses = new HashSet<>(classes);

        Set<PsiField> fieldsWithGetter = new HashSet<>();
        Set<PsiField> fieldsWithSetter = new HashSet<>();

        methods.forEach(it -> {
            if (!it.hasModifierProperty(PsiModifier.PUBLIC)) {
                return;
            }

            whoseGetter(it).ifPresent(fieldsWithGetter::add);
            whoseSetter(it).ifPresent(fieldsWithSetter::add);
        });

        filteredMethods =
            methods.stream()
                .filter(new ConstructorsFilter())
                .filter(new AbstractMethodsFilter())
                .filter(new StaticMethodsFilter())
                .filter(new GettersFilter())
                .filter(new SettersFilter())
                .filter(new NoTargetsMethodsFilter(this))
                .filter(new OverridingMethodsFilter())
                .filter(new OverriddenMethodsFilter())
                .filter(new PrivateMethodsCallersFilter())
                .filter(new PrivateFieldAccessorsFilter(fieldsWithGetter, fieldsWithSetter))
                .filter(new EmptyMethodsFilter())
                .filter(new ExceptionsThrowersFilter())
                .filter(new SimpleDelegationsFilter())
                .collect(Collectors.toList());
    }

    @NotNull
    public List<PsiJavaFile> getAllJavaFiles() {
        return allJavaFiles;
    }

    @NotNull
    public List<PsiJavaFile> getSourceJavaFiles() {
        return sourceJavaFiles;
    }

    @NotNull
    public List<PsiClass> getClasses() {
        return classes;
    }

    @NotNull
    public List<PsiMethod> getMethods() {
        return methods;
    }

    @NotNull
    public List<PsiMethod> getFilteredMethods() {
        return filteredMethods;
    }

    public @NotNull List<PsiClass> possibleTargets(final @NotNull PsiMethod method) {
        List<PsiClass> targets = new ArrayList<>();

        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            PsiType type = parameter.getType();
            if (!(type instanceof PsiClassType)) {
                continue;
            }

            PsiClassType classType = (PsiClassType) type;
            PsiClass actualClass = classType.resolve();

            if (
                actualClass != null &&
                allInterestingClasses.contains(actualClass) &&
                !actualClass.equals(method.getContainingClass())
            ) {
                targets.add(actualClass);
            }
        }

        return targets;
    }
}
