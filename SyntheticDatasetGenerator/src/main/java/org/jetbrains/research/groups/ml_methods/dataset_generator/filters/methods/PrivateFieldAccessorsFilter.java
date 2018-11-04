package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Predicate;

public class PrivateFieldAccessorsFilter implements Predicate<PsiMethod> {
    private final @NotNull Set<PsiField> fieldsWithGetter;

    private final @NotNull Set<PsiField> fieldsWithSetter;

    public PrivateFieldAccessorsFilter(
        final @NotNull Set<PsiField> fieldsWithGetter,
        final @NotNull Set<PsiField> fieldsWithSetter
    ) {
        this.fieldsWithGetter = fieldsWithGetter;
        this.fieldsWithSetter = fieldsWithSetter;
    }

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

                if (field.hasModifierProperty(PsiModifier.PUBLIC)) {
                    return;
                }

                if (isInLeftSideOfAssignment(expression)) {
                    if (!fieldsWithSetter.contains(field)) {
                        resultRef.set(false);
                    }
                } else {
                    if (!fieldsWithGetter.contains(field)) {
                        resultRef.set(false);
                    }
                }
            }
        }.visitElement(psiMethod);

        return resultRef.get();
    }

    private boolean isInLeftSideOfAssignment(final @NotNull PsiReferenceExpression expression) {
        PsiElement parent = expression.getParent();
        if (!(parent instanceof PsiAssignmentExpression)) {
            return false;
        }

        PsiAssignmentExpression assignment = (PsiAssignmentExpression) parent;
        return expression.equals(assignment.getLExpression());
    }
}
