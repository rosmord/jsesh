description = """
    New JSesh installer builder.

    Due to changes in Java distribution, we must a) bundle jre with
    JSesh and b) provide more "native" installers.

    The ultimate goal of this file is to provide an easy way to
    build the said installers.
  
    What we do currently: create two distinct folders, 
""".trimIndent()

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

// Skip publishing for installer module
tasks.withType<PublishToMavenRepository> {
    enabled = false
}

dependencies {
    implementation(project(":jseshAppli"))
    implementation(project(":jsesh"))
    implementation(project(":jseshGlyphs"))
    implementation(project(":signInfoAppli"))
}

// Map project properties to match Maven representation
val props = mapOf(
    "project.version" to project.version,
)

// Helper to expand properties
val expandLineRegex = Regex("""\$\{([\w.]+)\}""")
fun expandLine(line: String): String {
    return line.replace(expandLineRegex) {
        val varName = it.groupValues[1]
        val value = props[varName] ?: it.groupValues[0]
        "$value"
    }
}


// Define directories for Mac and Windows installers
val macInstallDir = layout.buildDirectory.dir("mac")
val macDir = macInstallDir.map { it.dir("JSesh-${project.version}") }
val windowsInstallDir = layout.buildDirectory.dir("windows")
val windowsDir = windowsInstallDir.map { it.dir("JSesh-${project.version}") }

// Task to copy resources for Mac
val copyResourcesMac by tasks.registering(Copy::class) {
    into(macDir)
    
    from("src/filtered") {
        filter( ::expandLine)
    }
    from("src/mac-filtered") {
        filter( ::expandLine)
    }
    from("src/binary")
    from("src/mac-binary")
}

// Task to copy resources for Windows
val copyResourcesWindows by tasks.registering(Copy::class) {
    into(windowsDir)
    
    from("src/windows-filtered") {
        filter( ::expandLine)
    }
    from("src/filtered") {
        filter( ::expandLine)
    }
    from("src/binary")
}

// Task to copy install files for Mac
val copyInstallMac by tasks.registering(Copy::class) {
    into(macInstallDir)
    
    from("src/mac-install") {
        filter( ::expandLine)
    }
}

// Task to copy install files for Windows
val copyInstallWindows by tasks.registering(Copy::class) {
    into(windowsInstallDir)
    
    from("src/windows-install") {
        filter( ::expandLine)
    }
}

// Task to copy dependencies for Mac
val copyDependenciesMac by tasks.registering(Copy::class) {
    dependsOn(copyResourcesMac)
    
    from(configurations.runtimeClasspath)
    into(macDir.map { it.dir("JSesh.app/Contents/lib") })
}

// Task to copy dependencies for Windows
val copyDependenciesWindows by tasks.registering(Copy::class) {
    dependsOn(copyResourcesWindows)
    
    from(configurations.runtimeClasspath)
    into(windowsDir.map { it.dir("lib") })
}

// Main assembly tasks
val assembleMac by tasks.registering {
    dependsOn(copyResourcesMac, copyInstallMac, copyDependenciesMac)
    group = "build"
    description = "Assembles Mac installer files"
}

val assembleWindows by tasks.registering {
    dependsOn(copyResourcesWindows, copyInstallWindows, copyDependenciesWindows)
    group = "build"
    description = "Assembles Windows installer files"
}

val assembleInstallers by tasks.registering {
    dependsOn(assembleMac, assembleWindows)
    group = "build"
    description = "Assembles both Mac and Windows installer files"
}

tasks.named("assemble") {
    dependsOn(assembleInstallers)
}
