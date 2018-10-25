package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class PrivateFieldAccessorsFilter implements Predicate<PsiMethod> {
    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        final Ref<Boolean> resultRef = new Ref<>(true);

        new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(
                final @NotNull PsiReferenceExpression expression
            ) {
                super.visitReferenceExpression(expression);

                JavaResolveResult resolveResult = expression.advancedResolve(false);
                PsiElement referencedElement = resolveResult.getElement();
                if (referencedElement == null) {
                    return;
                }

                if (!(referencedElement instanceof PsiField)) {
                    return;
                }

                PsiField field = (PsiField) referencedElement;

                if (!field.hasModifierProperty(PsiModifier.PUBLIC)) {
                    resultRef.set(false);
                }
            }
        }.visitElement(psiMethod);

        return resultRef.get();
    }
}
