package org.jetbrains.research.groups.ml_methods.github_repos_searcher;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GitHubReposSearcherApplication {
    private static final int maxPageSize = 100; // https://developer.github.com/v3/search/#search-repositories

    private static final int maxSizeOfResult = 1000; // https://developer.github.com/v3/search/#about-the-search-api

    public static void main(String[] args) {
        try {
            GitHubReposSearcherApplication application = new GitHubReposSearcherApplication();
            application.run();
        } catch (Exception e) {
            reportException(e, "An exception occurred!");
        }
    }

    private void run() {
        final GitHub github;
        try {
            github = GitHub.connectAnonymously();
        } catch (IOException e) {
            reportException(e, "Failed to connect to GitHub!");
            return;
        }

        PagedSearchIterable<GHRepository> iterable = createIterable(github);
        printIterableInfo(iterable);

        List<String> repositoriesUrl = extractRepositories(iterable);

        final String fileName = "repositories";

        try {
            saveResultsInFile(fileName, repositoriesUrl);
        } catch (IOException e) {
            reportException(e, "Failed to save repositories into file!");
            return;
        }

        System.out.println("done");
    }

    private @NotNull PagedSearchIterable<GHRepository> createIterable(final @NotNull GitHub github) {
        final String language = "Java";
        final String mustBeUpdatedSince = "2018-01-01";
        final int stars = 1000;

        System.out.printf(
            "Searching for public repositories with\n" +
            "main language \"%s\",\n" +
            "last update at least on %s,\n" +
            "at least %s stars...\n\n",
            language,
            mustBeUpdatedSince,
            stars
        );

        return github.searchRepositories()
                     .language(language)
                     .q("is:public") // repository is public
                     .pushed(">=" + mustBeUpdatedSince)
                     .stars(">=" + stars)
                     .sort(GHRepositorySearchBuilder.Sort.STARS)
                     .order(GHDirection.DESC)
                     .list().withPageSize(maxPageSize);
    }

    private void printIterableInfo(final @NotNull PagedSearchIterable<GHRepository> iterable) {
        int totalNumberOfFoundRepositories = iterable.getTotalCount();
        System.out.printf("%s repositories has been found\n", totalNumberOfFoundRepositories);
        if (iterable.isIncomplete()) {
            System.out.println("Results are incomplete, there might be others repositories");
        } else {
            System.out.println("These are all the repositories that match the requirements");
        }

        if (totalNumberOfFoundRepositories > maxSizeOfResult) {
            System.out.printf(
                "Only first %s repositories out of %s can be retrieved!" +
                " Repositories will be sorted by stars in descending order.\n\n",
                maxSizeOfResult,
                totalNumberOfFoundRepositories
            );
        }
    }

    private @NotNull List<String> extractRepositories(final @NotNull PagedSearchIterable<GHRepository> iterable) {
        List<String> repositoriesUrl = new ArrayList<>();
        iterable.forEach(ghRepository -> repositoriesUrl.add(ghRepository.getHttpTransportUrl()));

        return repositoriesUrl;
    }

    private void saveResultsInFile(
        final @NotNull String fileName,
        final @NotNull List<String> repositoriesUrl
    ) throws IOException {
        System.out.printf("Writing results into file named \"%s\"...\n", fileName);
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(fileName))) {
            repositoriesUrl.forEach(printWriter::println);
        }
    }

    private static void reportException(final @NotNull Exception exception, final @NotNull String message) {
        System.err.println(message + " " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
}
