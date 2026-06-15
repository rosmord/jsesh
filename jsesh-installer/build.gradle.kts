plugins {
    base  // provides clean and build lifecycle without java compilation
}

val jseshVersion = rootProject.version.toString()
val macDir = layout.buildDirectory.dir("mac/JSesh-$jseshVersion")
val windowsDir = layout.buildDirectory.dir("windows/JSesh-$jseshVersion")
val macInstallDir = layout.buildDirectory.dir("mac")
val windowsInstallDir = layout.buildDirectory.dir("windows")

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

// Use filter (simple string replacement) because source files use Maven-style
// ${project.version} tokens which Groovy's template engine cannot handle with dotted keys.
fun CopySpec.filterVersion() {
    filter { line: String -> line.replace("\${project.version}", jseshVersion) }
}

// ── Mac bundle ──────────────────────────────────────────────────────────────

val copyResourcesMac = tasks.register<Copy>("copyResourcesMac") {
    from("src/mac-filtered") { filterVersion() }
    from("src/mac-binary")
    from("src/binary")
    into(macDir)
}

val copyInstallMac = tasks.register<Copy>("copyInstallMac") {
    from("src/mac-install") { filterVersion() }
    into(macInstallDir)
}

val copyDepsMac = tasks.register<Copy>("copyDepsMac") {
    from(appJars)
    into(macDir.map { it.dir("JSesh.app/Contents/lib") })
}

// ── Windows bundle ───────────────────────────────────────────────────────────

val copyResourcesWindows = tasks.register<Copy>("copyResourcesWindows") {
    from("src/windows-filtered") { filterVersion() }
    from("src/binary")
    into(windowsDir)
}

val copyInstallWindows = tasks.register<Copy>("copyInstallWindows") {
    from("src/windows-install") { filterVersion() }
    into(windowsInstallDir)
}

val copyDepsWindows = tasks.register<Copy>("copyDepsWindows") {
    from(appJars)
    into(windowsDir.map { it.dir("lib") })
}

tasks.named("build") {
    dependsOn(copyResourcesMac, copyInstallMac, copyDepsMac,
              copyResourcesWindows, copyInstallWindows, copyDepsWindows)
}
