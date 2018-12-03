package org.jetbrains.research.groups.ml_methods.dataset_generator.rewriter;

import com.intellij.psi.*;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.ProjectInfo;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// https://intellij-support.jetbrains.com/hc/en-us/community/posts/360000140664-PsiElement-replace-proper-way-to-do-
// https://www.jetbrains.org/intellij/sdk/docs/tutorials/custom_language_support/quick_fix.html
// todo: qualifiers, setters, make static, leave method unchanged
public class MethodRewriter {
    private final @NotNull ProjectInfo projectInfo;

    public MethodRewriter(final @NotNull ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public void rewrite(final @NotNull PsiMethod psiMethod) {
        List<PsiReferenceExpression> allReferenceExpressions = new ArrayList<>();

        new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(
                final @NotNull PsiReferenceExpression expression
            ) {
                super.visitReferenceExpression(expression);
                allReferenceExpressions.add(expression);
            }
        }.visitElement(psiMethod);

        for (PsiReferenceExpression expression : allReferenceExpressions) {
            Optional<PsiField> optional = MethodUtils.referencedNonPublicField(expression);
            if (!optional.isPresent()) {
                return;
            }

            PsiField field = optional.get();

            if (MethodUtils.isInLeftSideOfAssignment(expression)) {
                // setter
            } else {
                // getter

                PsiMethod getter = projectInfo.getFieldToGetter().get(field);
                PsiExpression getterCallExpression =
                    PsiElementFactoryImpl.SERVICE.getInstance(projectInfo.getProject())
                        .createExpressionFromText(getter.getName() + "()", expression);

                expression.replace(getterCallExpression);
            }
        }
    }
}
