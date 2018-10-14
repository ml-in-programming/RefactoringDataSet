package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.psi.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MethodUtils {
    private static final @NotNull Logger LOGGER = Logger.getLogger(MethodUtils.class);

    static {
        LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p [%c.%M] - %m%n")));
    }

    private MethodUtils() { }

    public static @NotNull Optional<PsiField> whoseGetter(final @NotNull PsiMethod method) {
        if (method.getParameterList().getParametersCount() != 0) {
            return Optional.empty();
        }

        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return Optional.empty();
        }

        PsiStatement[] statements = body.getStatements();
        if (statements.length != 1) {
            return Optional.empty();
        }

        PsiStatement theStatement = statements[0];
        if (!(theStatement instanceof PsiReturnStatement)) {
            return Optional.empty();
        }

        PsiReturnStatement returnStatement = (PsiReturnStatement) theStatement;
        PsiExpression expression = returnStatement.getReturnValue();

        if (!(expression instanceof PsiReferenceExpression)) {
            return Optional.empty();
        }

        PsiReferenceExpression referenceExpression = (PsiReferenceExpression) expression;

        if (referenceExpression.isQualified()) {
            PsiExpression qualifierExpression = referenceExpression.getQualifierExpression();
            if (!(qualifierExpression instanceof PsiThisExpression)) {
                return Optional.empty();
            }
        }

        JavaResolveResult resolveResult = referenceExpression.advancedResolve(false);

        PsiElement referencedElement = resolveResult.getElement();
        if (referencedElement == null) {
            LOGGER.error("Getter like method but failed to resolve reference! " + fullyQualifiedName(method));
            return Optional.empty();
        }

        if (!(referencedElement instanceof PsiField)) {
            return Optional.empty();
        }

        PsiField field = (PsiField) referencedElement;

        PsiClass fieldClass = field.getContainingClass();
        if (fieldClass == null || !fieldClass.equals(method.getContainingClass())) { // what if it's just a super class?
            return Optional.empty();
        }

        if (!field.getType().equals(method.getReturnType())) {
            return Optional.empty();
        }

        return Optional.of(field);
    }

    public static @NotNull String fullyQualifiedName(final @NotNull PsiMethod method) {
        final PsiClass containingClass = method.getContainingClass();

        String className = "";
        if (containingClass != null) {
            className = containingClass.getQualifiedName();
            if (className == null) {
                className = containingClass.getName();
            }

            if (className == null) {
                className = "";
            }
        }

        return className + '.' + method.getName();
    }

    public static @NotNull List<PsiClass> possibleTargets(
        final @NotNull PsiMethod method,
        final @NotNull Set<PsiClass> allInterestingClasses
    ) {
        List<PsiClass> targets = new ArrayList<>();

        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            PsiType type = parameter.getType();
            if (!(type instanceof PsiClassType)) {
                continue;
            }

            PsiClassType classType = (PsiClassType) type;
            PsiClass actualClass = classType.resolve();

            if (allInterestingClasses.contains(actualClass)) {
                targets.add(actualClass);
            }
        }

        return targets;
    }
}