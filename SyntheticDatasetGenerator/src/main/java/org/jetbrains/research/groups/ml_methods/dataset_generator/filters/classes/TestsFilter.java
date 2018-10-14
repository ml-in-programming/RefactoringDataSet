package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.classes;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class TestsFilter implements Predicate<PsiClass> {
    @Override
    public boolean test(final @NotNull PsiClass psiClass) {
        String className = psiClass.getName();
        if (className == null) {
            return true;
        }

        className = className.toLowerCase();
        return !className.endsWith("test") && !className.endsWith("tests");
    }
}
