package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.filters.methods.EmptyMethodsFilter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Dataset {
    private final @NotNull List<PsiMethod> methods;

    private final @NotNull List<PsiClass> classes;

    private final @NotNull List<Point> points;

    private final @NotNull Map<PsiMethod, Integer> idOfMethod = new HashMap<>();

    public Dataset(final @NotNull ProjectInfo projectInfo) {
        methods = new ArrayList<>();
        classes = new ArrayList<>();
        points = new ArrayList<>();

        Set<PsiClass> allClasses =
            Stream.concat(
                projectInfo.getMethodsAfterFiltration()
                    .stream()
                    .flatMap(it -> projectInfo.possibleTargets(it).stream()),
                projectInfo.getMethodsAfterFiltration()
                    .stream()
                    .map(PsiMember::getContainingClass)
            ).collect(Collectors.toSet());

        Set<PsiMethod> allMethods =
            allClasses.stream()
                .flatMap(it -> Arrays.stream(it.getMethods()))
                .filter(new EmptyMethodsFilter())
                .collect(Collectors.toSet());

        Map<PsiClass, Integer> idOfClass = new HashMap<>();

        int methodId = 0;
        for (PsiMethod method : allMethods) {
            methods.add(method);
            idOfMethod.put(method, methodId);
            methodId++;
        }

        int classId = 0;
        for (PsiClass clazz : allClasses) {
            List<Integer> idsOfMethods = getIdsOfMethodsIn(clazz);

            if (idsOfMethods.isEmpty()) {
                continue;
            }

            classes.add(clazz);
            idOfClass.put(clazz, classId);
            classId++;
        }

        for (PsiMethod method : projectInfo.getMethodsAfterFiltration()) {
            points.add(new Point(
                idOfMethod.get(method),
                idOfClass.get(method.getContainingClass()),
                1
            ));

            for (PsiClass target : projectInfo.possibleTargets(method)) {
                if (!idOfClass.containsKey(target)) {
                    continue;
                }

                points.add(new Point(
                    idOfMethod.get(method),
                    idOfClass.get(target),
                    0
                ));
            }
        }
    }

    public List<Integer> getIdsOfMethodsIn(final @NotNull PsiClass psiClass) {
        List<Integer> idsOfMethods = new ArrayList<>();
        for (PsiMethod method : psiClass.getMethods()) {
            if (!idOfMethod.containsKey(method)) {
                continue;
            }

            idsOfMethods.add(idOfMethod.get(method));
        }

        return idsOfMethods;
    }

    public List<PsiMethod> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public List<PsiClass> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public static class Point {
        private final int methodId;

        private final int classId;

        private final int label;

        public Point(final int methodId, final int classId, final int label) {
            this.methodId = methodId;
            this.classId = classId;
            this.label = label;
        }

        public int getMethodId() {
            return methodId;
        }

        public int getClassId() {
            return classId;
        }

        public int getLabel() {
            return label;
        }
    }
}
