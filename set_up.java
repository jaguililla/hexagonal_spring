///usr/bin/env java --enable-preview --source 21 -cp "*" "$0" "$@" ; exit $?

import static java.util.Map.entry;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

Scanner scanner = new Scanner(System.in);
FileSystem fs = FileSystems.getDefault();

void main() {
    var deletions =
        Stream.of(
            entry(".github", prompt("Keep GitHub workflows and templates (Yn): ", "y")),
            entry(".gitlab*", prompt("Keep GitLab workflows and templates (Yn): ", "y")),
            entry("CODE_OF_CONDUCT.md", prompt("Keep 'CODE_OF_CONDUCT.md' file (Yn): ", "y")),
            entry("CONTRIBUTING.md", prompt("Keep 'CONTRIBUTING.md' file (Yn): ", "y")),
            entry("LICENSE.md", prompt("Keep 'LICENSE.md' file (Yn): ", "y")),
//            entry(".git", prompt("Keep Git history (Yn): ", "y")),
            entry("set_up.java", prompt("Keep this set up file 'set_up.java' file (Yn): ", "y"))
        )
        .filter(it -> it.getValue().equalsIgnoreCase("n"))
        .toList();

    // TODO Rename artifacts, groups or base package
    // TODO Show summary before applying changes

    deletions.forEach(it -> delete(it.getKey()));
}

String prompt(String message, String defaultValue) {
    System.out.print(message);
    var v = scanner.nextLine();
    return v == null || v.isBlank() ? defaultValue : v;
}

void delete(String glob) {
    var matcher = fs.getPathMatcher("glob:./" + glob);
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
                System.out.println("Error deleting " + it);
            }
        });
}
