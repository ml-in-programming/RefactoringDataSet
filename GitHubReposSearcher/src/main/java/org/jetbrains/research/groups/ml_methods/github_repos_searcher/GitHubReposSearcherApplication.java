package org.jetbrains.research.groups.ml_methods.github_repos_searcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.github_repos_searcher.properties.DefaultProperties;
import org.jetbrains.research.groups.ml_methods.github_repos_searcher.properties.Properties;
import org.kohsuke.github.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GitHubReposSearcherApplication {
    private static final int maxPageSize = 100; // https://developer.github.com/v3/search/#search-repositories

    private static final int maxSizeOfResult = 1000; // https://developer.github.com/v3/search/#about-the-search-api

    private static final @NotNull String destinationFileName = "repositories";

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

        PagedSearchIterable<GHRepository> iterable = createIterable(github, DefaultProperties.getInstance());
        printIterableInfo(iterable);

        List<String> repositoriesUrl = extractRepositories(iterable);

        try {
            saveResultsInFile(destinationFileName, repositoriesUrl);
        } catch (IOException e) {
            reportException(e, "Failed to save repositories into file!");
            return;
        }

        System.out.println("done");
    }

    private @NotNull PagedSearchIterable<GHRepository> createIterable(
        final @NotNull GitHub github,
        final @NotNull Properties properties
    ) {
        final String language = properties.getLanguage();
        final String mustBeUpdatedSinceDate = properties.getMustBeUpdatedSinceDate();
        final int starsLowerBound = properties.getStarsLowerBound();

        System.out.printf(
            "Searching for public repositories with\n" +
            "main language \"%s\",\n" +
            "last update at least on %s,\n" +
            "at least %s stars...\n\n",
            language,
            mustBeUpdatedSinceDate,
            starsLowerBound
        );

        return github.searchRepositories()
                     .language(language)
                     .q("is:public") // repository is public
                     .pushed(">=" + mustBeUpdatedSinceDate)
                     .stars(">=" + starsLowerBound)
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
        int totalNumberOfRepositories = Math.min(iterable.getTotalCount(), maxSizeOfResult);
        List<String> repositoriesUrl = new ArrayList<>();

        System.out.printf("0 / %s", totalNumberOfRepositories);

        iterable.forEach(
            new Consumer<GHRepository>() {
                int numberOfCollectedRepositories = 0;

                @Override
                public void accept(GHRepository ghRepository) {
                    repositoriesUrl.add(ghRepository.getHttpTransportUrl());
                    numberOfCollectedRepositories++;

                    System.out.printf("\r%s / %s", numberOfCollectedRepositories, totalNumberOfRepositories);
                }
            }
        );

        System.out.print("\r");

        return repositoriesUrl;
    }

    private void saveResultsInFile(
        final @NotNull String fileName,
        final @NotNull List<String> repositoriesUrl
    ) throws IOException {
        System.out.printf("Writing results into file named \"%s\"...\n", fileName);
        Files.write(Paths.get(fileName), repositoriesUrl);
    }

    private static void reportException(final @NotNull Exception exception, final @NotNull String message) {
        System.err.println(message + " " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
}
