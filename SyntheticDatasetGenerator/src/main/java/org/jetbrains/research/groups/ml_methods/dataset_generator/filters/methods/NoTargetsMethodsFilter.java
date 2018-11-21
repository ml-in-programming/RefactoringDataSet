package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.ProjectInfo;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils;

import java.util.Set;
import java.util.function.Predicate;

public class NoTargetsMethodsFilter implements Predicate<PsiMethod> {
    private final @NotNull ProjectInfo projectInfo;

    public NoTargetsMethodsFilter(final @NotNull ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        return !projectInfo.possibleTargets(psiMethod).isEmpty();
    }
}
