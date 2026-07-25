description = """
    New JSesh installer builder.

    Due to changes in Java distribution, we must a) bundle jre with
    JSesh and b) provide more "native" installers.

    The ultimate goal of this file is to provide an easy way to
    build the said installers.
  
    What we do currently: create two distinct folders, 
""".trimIndent()

plugins {
    base  // provides clean and build lifecycle without java compilation
    id("jsesh.common")
}

val standardDir = layout.buildDirectory.dir("standard")

// Collect all runtime jars from jseshAppli (includes transitive deps)
val appJars: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

dependencies {
    appJars(project(":jseshAppli"))
    appJars(project(":jsesh"))
    appJars(project(":jseshGlyphs"))
    appJars(project(":signInfoAppli"))
}


val copyResourcesStandard = tasks.register<Copy>("copyResourcesStandard") {  
    into(standardDir)
      from ("src/binary/texts") {
        into("jsesh-texts")
    }
}

tasks.named("build") {
    dependsOn(copyResourcesStandard)
}

