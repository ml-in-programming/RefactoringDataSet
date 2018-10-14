package org.jetbrains.research.groups.ml_methods.dataset_generator.filters;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class OverriddenMethodsFilter implements Predicate<PsiMethod> {
    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        final PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            throw new IllegalStateException();
        }

        final Query<PsiMethod> query = OverridingMethodsSearch.search(psiMethod);
        return query.findFirst() == null;
    }
}
