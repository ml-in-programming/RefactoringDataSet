package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.ProjectInfo;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class PrivateFieldAccessorsFilter implements Predicate<PsiMethod> {
    private final @NotNull ProjectInfo projectInfo;

    public PrivateFieldAccessorsFilter(final @NotNull ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
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

                Optional<PsiField> optional = MethodUtils.referencedNonPublicField(expression);
                if (!optional.isPresent()) {
                    return;
                }

                PsiField field = optional.get();

                if (MethodUtils.isInLeftSideOfAssignment(expression)) {
                    if (!projectInfo.getFieldToSetter().containsKey(field)) {
                        resultRef.set(false);
                    }
                } else {
                    if (!projectInfo.getFieldToGetter().containsKey(field)) {
                        resultRef.set(false);
                    }
                }
            }
        }.visitElement(psiMethod);

        return resultRef.get();
    }
}
