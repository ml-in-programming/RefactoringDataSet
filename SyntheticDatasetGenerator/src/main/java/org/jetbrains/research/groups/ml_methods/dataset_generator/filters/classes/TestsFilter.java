package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.classes;

import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static org.jetbrains.research.groups.ml_methods.dataset_generator.filters.utils.JavaFileUtils.getDirectoryWithRootPackageFor;

public class TestsFilter implements Predicate<PsiClass> {
    @Override
    public boolean test(final @NotNull PsiClass psiClass) {
        VirtualFile virtualFile = psiClass.getContainingFile().getVirtualFile();
        if (TestSourcesFilter.isTestSources(virtualFile, psiClass.getProject())) {
            return false;
        }

        PsiJavaFile file = (PsiJavaFile) psiClass.getContainingFile();
        if (file != null && (!testFile(file) || !testPackage(file))) {
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
        PsiDirectory directory = getDirectoryWithRootPackageFor(file);

        while (directory != null) {
            String dirName = directory.getName().toLowerCase();
            if (dirName.equals("test") || dirName.equals("tests")) {
                return false;
            }

            directory = directory.getParentDirectory();
        }

        return true;
    }

    private boolean testPackage(final @NotNull PsiJavaFile file) {
        String packageName = file.getPackageName();
        String[] packageSequence;

        if ("".equals(packageName)) {
            packageSequence = new String[0];
        } else {
            packageSequence = packageName.split("\\.");
        }

        for (String packageElement : packageSequence) {
            String str = packageElement.toLowerCase();
            if (str.equals("test") || str.equals("tests")) {
                return false;
            }
        }

        return true;
    }
}
