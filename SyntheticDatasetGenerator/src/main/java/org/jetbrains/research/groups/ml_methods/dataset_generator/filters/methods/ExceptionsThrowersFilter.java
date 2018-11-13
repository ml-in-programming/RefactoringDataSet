package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiThrowStatement;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

import static org.jetbrains.research.groups.ml_methods.dataset_generator.filters.utils.MethodUtils.getSingleStatementOf;

public class ExceptionsThrowersFilter implements Predicate<PsiMethod> {
    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        Optional<PsiStatement> optionalStatement = getSingleStatementOf(psiMethod);

        if (!optionalStatement.isPresent()) {
            return true;
        }

        PsiStatement statement = optionalStatement.get();
        return !(statement instanceof PsiThrowStatement);
    }
}
