package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleDelegationsFilter implements Predicate<PsiMethod> {
    @Override
    public boolean test(final @NotNull PsiMethod psiMethod) {
        Set<PsiParameter> parameters =
            Arrays.stream(psiMethod.getParameterList().getParameters())
                .collect(Collectors.toSet());

        PsiCodeBlock body = psiMethod.getBody();
        if (body == null) {
            return true;
        }

        PsiStatement[] statements = body.getStatements();
        if (statements.length != 1) {
            return true;
        }

        PsiStatement theStatement = statements[0];
        if (theStatement instanceof PsiReturnStatement) {
            PsiReturnStatement returnStatement = (PsiReturnStatement) theStatement;
            PsiExpression expression = returnStatement.getReturnValue();

            if (!(expression instanceof PsiMethodCallExpression)) {
                return true;
            }

            return testMethodCall((PsiMethodCallExpression) expression, parameters);
        }

        if (theStatement instanceof PsiExpressionStatement) {
            PsiExpression expression = ((PsiExpressionStatement) theStatement).getExpression();
            if (!(expression instanceof PsiMethodCallExpression)) {
                return true;
            }

            return testMethodCall((PsiMethodCallExpression) expression, parameters);
        }

        return true;
    }

    private boolean testMethodCall(
        final @NotNull PsiMethodCallExpression methodCall,
        final @NotNull Set<PsiParameter> parameters
    ) {
        PsiExpression qualifierExpression =
            methodCall.getMethodExpression().getQualifierExpression();

        if (!isSimpleQualifier(qualifierExpression, parameters)) {
            return true;
        }

        for (PsiExpression argument : methodCall.getArgumentList().getExpressions()) {
            if (!isParameter(argument, parameters)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSimpleQualifier(
        final @Nullable PsiExpression qualifierExpression,
        final @NotNull Set<PsiParameter> parameters
    ) {
        if (qualifierExpression == null || qualifierExpression instanceof PsiThisExpression) {
            return true;
        }

        if (!(qualifierExpression instanceof PsiReferenceExpression)) {
            return false;
        }

        PsiReferenceExpression referenceExpression = (PsiReferenceExpression) qualifierExpression;
        JavaResolveResult resolveResult = referenceExpression.advancedResolve(false);

        return parameters.contains(resolveResult.getElement());
    }

    private boolean isParameter(
        final @NotNull PsiExpression expression,
        final @NotNull Set<PsiParameter> parameters
    ) {
        if (!(expression instanceof PsiReferenceExpression)) {
            return false;
        }

        PsiReferenceExpression referenceExpression = (PsiReferenceExpression) expression;
        JavaResolveResult resolveResult = referenceExpression.advancedResolve(false);

        return parameters.contains(resolveResult.getElement());
    }
}
