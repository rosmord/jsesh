description = "An application to edit signs properties."

plugins {
    id("jsesh.java-conventions")
    application
    id("org.beryx.runtime") version "2.0.1"
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshGlyphs"))
    implementation(project(":jhotdrawfw"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation(project(":jseshLabels"))
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
}


application {
    mainClass.set("jsesh.utilitysoftwares.signinfoeditor.Main")
    applicationName = "JSesh SignInfoEditor"
    applicationDefaultJvmArgs = listOf("-Xmx2G")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}


// Native installer, built with jpackage through the beryx runtime plugin.

val installerAppName = "JSeshSignInfo"
val fullName = "${installerAppName}-${project.version}"
val linuxPackageName = installerAppName.lowercase()
val macBundleIdentifier = "org.qenherkhopeshef.jsesh.signinfo"
// The license files are the ones of the JSesh installer.
val licenseDir = rootDir.resolve("jsesh-installer/src/main/binary")
val prepackageDir = layout.buildDirectory.dir("prepackage")
val os = org.gradle.internal.os.OperatingSystem.current()

// Resources for jpackage's --resource-dir: jpackage only picks them up
// if they are named after the application image.
val copyPackagingResources = tasks.register<Copy>("copyPackagingResources") {
    into(prepackageDir)
    if (os.isWindows) {
        from("src/main/packaging/windows/canard.ico") {
            rename { "${fullName}.ico" }
        }
    } else if (os.isMacOsX) {
        from("src/main/packaging/mac/canard.icns") {
            rename { "${fullName}.icns" }
        }
        from("src/main/packaging/mac-filtered") {
            expand(
                mapOf(
                    "version" to project.version.toString().replace("-SNAPSHOT", ""),
                    "fullName" to fullName,
                    "appName" to installerAppName,
                    "bundleIdentifier" to macBundleIdentifier
                )
            )
        }
    } else if (os.isLinux) {
        from("src/main/packaging/linux/canard.png") {
            rename { "${fullName}.png" }
        }
        // The .desktop file must be named after the application, and point to its install dir.
        from("src/main/packaging/linux-filtered/SignInfo.desktop") {
            rename { "${fullName}.desktop" }
            expand(mapOf("fullName" to fullName, "linuxPackageName" to linuxPackageName))
        }
        from(licenseDir.resolve("LICENSE.txt")) {
            rename { "copyright" }
        }
    }
}

tasks.named("jpackageImage") {
    dependsOn(copyPackagingResources)
}


runtime {
    options.set(listOf("--strip-debug", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "java.logging"))

    jpackage {
        appVersion = project.version.toString().replace("-SNAPSHOT", "") // jpackage rejects the "-SNAPSHOT" suffix
        imageName = fullName
        installerName = installerAppName
        resourceDir = prepackageDir.get().asFile
        jvmArgs = application.applicationDefaultJvmArgs.toList()

        val type = project.findProperty("installerType") as String?
        if (type != null) {
            installerType = type
        }

        installerOptions.addAll(
            listOf(
                "--verbose",
                "--description", project.description.toString(),
                "--copyright", "Serge Rosmorduc, CeCILL-C license"
            )
        )

        if (os.isWindows) {
            if (installerType == null) {
                installerType = "msi"
            }
            installerOptions.addAll(listOf(
                "--win-dir-chooser",
                "--win-menu",
                "--win-shortcut",
                "--license-file", licenseDir.resolve("LICENSE.txt").path
            ))
        } else if (os.isLinux) {
            if (installerType == null) {
                installerType = "deb"
            }
            installerOptions.addAll(listOf(
                "--linux-shortcut",
                "--linux-package-name", linuxPackageName
            ))
        } else if (os.isMacOsX) {
            if (installerType == null) {
                installerType = "dmg"
            }
            installerOptions.addAll(listOf(
                "--mac-package-identifier", macBundleIdentifier,
                "--mac-package-name", installerAppName
            ))
            if (installerType == "dmg") {
                installerOptions.addAll(listOf(
                    "--mac-dmg-content", licenseDir.resolve("licenses").path,
                    "--mac-dmg-content", licenseDir.resolve("FONT-LICENSE.md").path,
                    "--mac-dmg-content", licenseDir.resolve("LICENSE.txt").path
                ))
            }
        }
    }
}
