package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class OverridingMethodsFilter implements Predicate<PsiMethod> {
    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        return AnnotationUtil.findAnnotation(psiMethod, "Override") == null &&
                psiMethod.findSuperMethods().length == 0;
    }
}
