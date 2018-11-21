package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils;

import java.util.Set;
import java.util.function.Predicate;

public class NoTargetsMethodsFilter implements Predicate<PsiMethod> {
    private final @NotNull Set<PsiClass> allClasses;

    public NoTargetsMethodsFilter(final @NotNull Set<PsiClass> allClasses) {
        this.allClasses = allClasses;
    }

    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        return !MethodUtils.possibleTargets(psiMethod, allClasses).isEmpty();
    }
}
