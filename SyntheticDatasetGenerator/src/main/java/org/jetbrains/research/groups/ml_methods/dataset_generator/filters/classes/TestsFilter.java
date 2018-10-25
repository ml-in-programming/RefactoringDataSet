package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.classes;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class TestsFilter implements Predicate<PsiClass> {
    @Override
    public boolean test(final @NotNull PsiClass psiClass) {
        PsiJavaFile file = (PsiJavaFile) psiClass.getContainingFile();
        if (file != null && !testFile(file)) {
            return false;
        }

        return testClass(psiClass);
    }

    private boolean testClass(final @NotNull PsiClass psiClass) {
        if (!testClassName(psiClass)) {
            return false;
        }

        PsiClass containingClass = psiClass.getContainingClass();
        if (containingClass == null) {
            return true;
        }

        return testClass(containingClass);
    }

    private boolean testClassName(final @NotNull PsiClass psiClass) {
        String className = psiClass.getName();
        if (className == null) {
            return true;
        }

        className = className.toLowerCase();
        return !className.endsWith("test") && !className.endsWith("tests");
    }

    private boolean testFile(final @NotNull PsiJavaFile file) {
        String packageName = file.getPackageName();
        String[] packageSequence;

        if ("".equals(packageName)) {
            packageSequence = new String[0];
        } else {
            packageSequence = packageName.split("\\.");
        }

        ArrayUtils.reverse(packageSequence);

        PsiDirectory directory = file.getParent();
        if (directory == null) {
            throw new IllegalStateException("File has no parent directory");
        }

        for (String packagePart : packageSequence) {
            if (!packagePart.equals(directory.getName())) {
                throw new IllegalStateException("Directories structure doesn't match with package");
            }

            directory = directory.getParentDirectory();
            if (directory == null) {
                throw new IllegalStateException("Directories structure doesn't match with package");
            }
        }

        while (directory != null) {
            String dirName = directory.getName().toLowerCase();
            if (dirName.equals("test") || dirName.equals("tests")) {
                return false;
            }

            directory = directory.getParentDirectory();
        }

        return true;
    }
}
