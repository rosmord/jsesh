package org.qenherkhopeshef.jsesh.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Gradle plugin that makes {@link CupTask} and {@link LexTask} available and creates
 * the {@code cuptools} configuration used to supply the cupAndlex tool jar.
 */
public class CupAndLexPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getConfigurations().create("cuptools", config -> {
            config.setDescription("Classpath for CUP parser generator and JLex lexer generator");
            config.setVisible(false);
        });
    }
}
