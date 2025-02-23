///usr/bin/env java --enable-preview --source 21 -cp "*" "$0" "$@" ; exit $?

import static java.util.Map.entry;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

Scanner scanner = new Scanner(System.in);
FileSystem fs = FileSystems.getDefault();

void main() {
    separator("--- DELETE COMPONENTS ---");

    var deletions =
        Stream.of(
            entry(".github", prompt("Delete GitHub workflows and templates (yN): ", "n")),
            entry(".gitlab*", prompt("Delete GitLab workflows and templates (yN): ", "n")),
            entry("CODE_OF_CONDUCT.md", prompt("Delete 'CODE_OF_CONDUCT.md' file (yN): ", "n")),
            entry("CONTRIBUTING.md", prompt("Delete 'CONTRIBUTING.md' file (yN): ", "n")),
            entry("LICENSE.md", prompt("Delete 'LICENSE.md' file (yN): ", "n")),
            entry(".git", prompt("Delete Git history (yN): ", "n")),
            entry("set_up.java", prompt("Delete this set up file 'set_up.java' file (yN): ", "n"))
        )
        .filter(it -> it.getValue().equalsIgnoreCase("y"))
        .toList();

    // TODO Rename organization, repository, artifact, group and base package (from most
    //  specific to less specific)
    //  To find out places to change, search for 'jaguililla' in the project

    separator("--- CONFIRM ---");

    var deletionSummary = deletions.stream().map(Entry::getKey).collect(joining(", "));
    var confirm = prompt(
        """
        Are you sure you want to:
        * Delete the following files/directories: %s
        * Rename %s to %s
        """.formatted(deletionSummary, "a", "b"),
        "n"
    );

    if (confirm.equalsIgnoreCase("n"))
        System.exit(0);

    deletions.forEach(it -> delete(it.getKey()));
}

void separator(String message) {
    System.out.printf("\n%s\n\n", message);
}

String prompt(String message, String defaultValue) {
    System.out.print(message);
    var v = scanner.nextLine();
    return v == null || v.isBlank() ? defaultValue : v;
}

void delete(String glob) {
    var matcher = fs.getPathMatcher("glob:./%s".formatted(glob));
    var cwd = new File(".");
    Optional
        .ofNullable(cwd.listFiles(it -> matcher.matches(it.toPath())))
        .map(List::of)
        .orElse(List.of())
        .forEach(it -> {
            try (Stream<Path> paths = Files.walk(it.toPath())) {
                paths
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(System.out::println); // (File::delete);
            }
            catch (IOException _) {
                System.out.printf("Error deleting %s%n", it);
            }
        });
}
