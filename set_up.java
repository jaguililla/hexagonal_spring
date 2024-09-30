///usr/bin/env java --enable-preview --source 21 -cp "*" "$0" "$@" ; exit $?

import static java.util.Map.entry;

import java.io.Console;
import java.util.List;

private Console console = System.console();

void main() {
    var options = List.of(
        entry("GitHub", prompt("Keep GitHub workflows and templates: (Yn)", "y")),
        entry("GitLab", prompt("Keep GitLab workflows and templates: (Yn)", "y")),
        entry("GitLab", prompt("Keep 'CODE_OF_CONDUCT.md' file: (Yn)", "y")),
        entry("GitLab", prompt("Keep 'CONTRIBUTING.md' file: (Yn)", "y")),
        entry("GitLab", prompt("Keep 'LICENSE.md' file: (Yn)", "y")),
        entry("GitLab", prompt("Keep this set up file 'set_up.java' file: (Yn)", "y"))
    );

    // Rename artifacts, groups or base package
    // Restart Git history (or delete it)
}

public String prompt(String message, String defaultValue) {
    console.printf(message);
    var v = console.readLine();
    return v == null || v.isBlank() ? defaultValue : v;
}
