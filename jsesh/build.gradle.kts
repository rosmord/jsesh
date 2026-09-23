import org.gradle.plugins.ide.eclipse.model.SourceFolder
import org.gradle.plugins.ide.eclipse.model.Classpath

description = "The core JSesh library"

plugins {
    java
    id("jsesh.java-conventions")
}

dependencies {
    implementation(project(":qenherkhopeshefUtils"))
    implementation(project(":jseshLabels"))
    implementation(project(":cupruntime"))
    implementation(project(":mdwlexer"))
    implementation(libs.itext)
    implementation(libs.jvectclipboard)
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
}

// Pass the build directory to the tests, so that they can create files there if needed.
// used to test bitmap creation.
tasks.test {
    systemProperty("buildDir", layout.buildDirectory.get().asFile.absolutePath)
}

val projectVersionToken = version.toString()

// Filtered resources: replace ${project.version} (Maven token) with the actual version
tasks.processResources {
    from("src/main/filtered-resources") {
        filter { line -> line.replace("\${project.version}", projectVersionToken) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

eclipse {
    classpath {
        file.whenMerged (Action<Classpath> {
            val generatedSourcesDir = layout.buildDirectory.dir("generated-sources").get().asFile
            generatedSourcesDir.listFiles() {
                f -> f.isDirectory
            } .forEach {
                d -> entries.add(SourceFolder(project.relativePath(d), null))
            }
            val generatedResourcesDir = layout.buildDirectory.dir("resources/main").get().asFile
            entries.add(SourceFolder(project.relativePath(generatedResourcesDir), null))
        })
    }
}
