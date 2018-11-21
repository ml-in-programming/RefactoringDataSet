package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils;

import java.util.function.Predicate;

public class SettersFilter implements Predicate<PsiMethod> {
    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        return !MethodUtils.whoseSetter(psiMethod).isPresent();
    }
}
