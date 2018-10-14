package org.jetbrains.research.groups.ml_methods.dataset_generator.filters;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class OverridingMethodsFilter implements Predicate<PsiMethod> {
    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        return psiMethod.findSuperMethods().length == 0;
    }
}
