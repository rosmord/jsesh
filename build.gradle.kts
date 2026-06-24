import org.gradle.plugins.ide.eclipse.model.EclipseModel
//import org.gradle.api.artifacts.VersionCatalogsExtension

description = "JSesh complete distribution"


plugins {
    base
    id("eclipse")
}


tasks.named("clean") {
    dependsOn(subprojects.map { subproject ->
        subproject.tasks.matching { task -> task.name == "clean" }
    })
}

tasks.register<Sync>("syncGlyphs") {
    group = "maintenance"
    description = "Copy glyphs from the JSesh fonts to the JSesh resources"
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(project.properties["fontSrc"] as String) {
        include("ManuelDeCodage/**/*.svg")
        eachFile {
            relativePath = RelativePath(true, name)
        }
    }
    from(project.properties["fontSrc"] as String) {
        include("Distribution/**/*.svg")
        eachFile {
            relativePath = RelativePath(true, name)
        }
    }
    into(project.properties["fontDest"] as String)
    doLast {
        println("Updated sign list")
        println("duplicate warnings are expected")
    }

}


tasks.register<Sync>("syncTexts") {
    group = "maintenance"
    description = "Update JSesh text lists"
    from(project.properties["textSrc"] as String)
    into(project.properties["textDest"] as String)
    doLast {
        println("updated texts list")
    }
}


tasks.register("copyDescription") {
    group = "maintenance"
    description = "update the sign description file"
        val srcFile = file("${project.property("glyphDocSrc")}/signs_description.xml")
    val destFile = file("${project.property("glyphDocDest")}/signs_description.xml")

    inputs.file(srcFile)
    outputs.file(destFile)

    doLast {
        srcFile.copyTo(destFile, overwrite = true)
        println("copied sign description")
    }
}


// This task is used to copy resources I develop independently:
// - the JSesh sign descriptions
// - the JSesh fonts
// - the JSesh texts
// Basically, it's not useful if you are not JSesh main developper.
tasks.register("prepareResources") {
    dependsOn("syncGlyphs", "syncTexts", "copyDescription")
    group = "maintenance"
    description = "Copy outer resources to JSesh code - used locally (see comments in build.gradle.kts)"    
}


tasks.register("distClean") {
    dependsOn("clean", subprojects.map { subproject ->
        subproject.tasks.matching { task -> task.name == "distClean" }
    })
    doLast {
        delete("bin")
        delete(".classpath")
        delete(".gradle")
        delete(".nb-gradle")
        delete(".project")
        delete(".settings")
        delete(".vscode")
        delete(".DS_Store")
        delete(".idea")
    }
}


allprojects {
    apply(plugin = "eclipse")

    configure<EclipseModel> {
        project {
            name = "${rootProject.name}-${project.name}"
        }
        classpath {
            isDownloadSources = true
            isDownloadJavadoc = false
        }
    }
}
