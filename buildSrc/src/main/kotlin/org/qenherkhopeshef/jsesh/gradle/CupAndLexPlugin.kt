package org.qenherkhopeshef.jsesh.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin that makes [CupTask] and [LexTask] available and creates
 * the `cuptools` configuration used to supply the cupAndlex tool jar.
 */
class CupAndLexPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val config = project.configurations.create("cuptools")
        config.description = "Classpath for CUP parser generator and JLex lexer generator"
        config.isVisible = false
    }
}
