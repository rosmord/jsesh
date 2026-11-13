description = """
    Installer for JSesh, using jpackage.

    The idea is to generate a native installer, and even to have github generate it for us.

    In practice, it demands a lot of care depending on the target platform.

    See also https://docs.oracle.com/en/java/javase/25/jpackage/index.html

    see https://badass-runtime-plugin.beryx.org/releases/latest/ for beryx plugin documentation

    Problem with files associations:
        see https://bugs.openjdk.org/browse/JDK-8372753
    """

plugins {
    base  // provides clean and build lifecycle without java compilation
    id("jsesh.common")
    id("org.beryx.runtime") version "2.0.1" 
    application
}

val applicationName= "JSesh"

// val jseshVersion = rootProject.version.toString()
// val macDir = layout.buildDirectory.dir("mac/JSesh-$jseshVersion")
// val windowsDir = layout.buildDirectory.dir("windows/JSesh-$jseshVersion")
// val macInstallDir = layout.buildDirectory.dir("mac")
// val windowsInstallDir = layout.buildDirectory.dir("windows")

dependencies {
    implementation(project(":jseshAppli"))
}


application {
    mainClass = "jsesh.jhotdraw.Main"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}


// Configure a copyResources task according to the operating system
// the files copied by this task will be used by jpackageImage

if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
    // WINDOWS
    val fullName = "${applicationName}-${project.version}"
    val copyResources = tasks.register<Copy>("copyResources") {
        from("src/main/packaging/windows/hibou.ico") {
            rename { "${fullName}.ico" }
        }
        into("build/prepackage")

    }

    // File associations: jpackage ignores --file-associations when it packages
    // an existing app image (JDK-8372753), which is how the beryx plugin builds
    // installers. So, on Windows, we build the installer ourselves, straight
    // from the jars and the runtime image made by beryx (skipInstaller is set
    // in the runtime block below).
    val jpackageWindowsInstaller = tasks.register<Exec>("jpackageWindowsInstaller") {
        group = "build"
        description = "Builds the Windows installer, with file associations."
        dependsOn("jre", "installDist", "copyResources")
        val type = (project.findProperty("installerType") as String?) ?: "msi"
        val destDir = layout.buildDirectory.dir("jpackage")
        doFirst {
            // jpackage refuses to overwrite an existing installer.
            delete(fileTree(destDir) { include("*.${type}") })
        }
        executable = file("${System.getProperty("java.home")}/bin/jpackage.exe").path
        args(
            "--type", type,
            "--dest", destDir.get().asFile.path,
            "--name", applicationName,
            "--app-version", project.version.toString().replace("-SNAPSHOT", ""),
            "--input", "build/install/jsesh-installer/lib",
            "--main-jar", tasks.jar.get().archiveFileName.get(),
            "--main-class", application.mainClass.get(),
            "--runtime-image", "build/jre",
            "--java-options", "-Xmx2G",
            "--icon", "src/main/packaging/windows/hibou.ico",
            "--copyright", "Serge Rosmorduc, CeCILL-C license",
            //"--win-per-user-install", would install in C:\Users\user-name\AppData\Local\application-name
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut",
            "--file-associations", "src/main/packaging/windows/jsesh-file-association.properties",
            "--file-associations", "src/main/packaging/windows/jsesh-hie-association.properties",
            "--license-file", "src/main/binary/LICENSE.txt",
            // app-content is not compatible with msi :-()
            "--verbose"
        )
    }
    tasks.named("jpackage") {
        dependsOn(jpackageWindowsInstaller)
    }
    /*
    val copyResources = tasks.register<Copy>("copyResources") {
        from("src/main/packaging/windows")
        into("build/prepackage")        
    }

    val copyWinExtras by tasks.registering(Copy::class) {
        dependsOn("jpackageImage")
        from("src/main/dist")
        into(layout.buildDirectory.dir("jpackage/${applicationName}-${project.version}"))
    }

    tasks.named("jpackage") {
        dependsOn(copyWinExtras)
    }
    */
} else if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
    // MAC OS X

    val fullName = "${applicationName}-${project.version}"
    val copyResources = tasks.register<Copy>("copyResources") {
        from("src/main/packaging/mac/hibou.icns") {
            rename { "${fullName}.icns" }
        }
        into("build/prepackage")
        from("src/main/packaging/mac-filtered") {
            expand(
                mapOf(
                    "version" to project.version.toString().replace("-SNAPSHOT", ""),
                    "fullName" to fullName
                )
            )
        }
        doLast {
            println("Copied resources to build/prepackage")
        }
    }

    // This part is due to a limitation of jpackage
    // jpackage only handle predefined files for resources.
    // for instance, it will copy the icon for the application,
    // but not the icon for documents.
    tasks.named("jpackageImage") {
        doLast {
            val appFolder = file("build/jpackage/${applicationName}-${project.version}.app")
            if (appFolder.exists()) {
                copy {
                    from("src/main/packaging/mac/canard.icns")
                    into(appFolder.resolve("Contents/Resources"))
                }
            } else {
                println("Problem : app folder not built")
            }
        }
    }
} else if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
    // Linux
    val fullName = "${applicationName}-${project.version}"
    val copyResources = tasks.register<Copy>("copyResources") {
        from("src/main/packaging/linux/hibou.png") {
            rename { "${fullName}.png" }
        }
        // To install icons in linux bar, we need to define a .desktop
        // it must be called after the app full name,
        // AND it must contain the right content, hence the rewriting with expand. 
        from("src/main/packaging/linux-filtered/JSesh.desktop") {
            rename { "${fullName}.desktop" }
            expand(mapOf("fullName" to fullName))
        }
        from("src/main/binary/LICENSE.txt") {
            rename {"copyright"}
        }
        // File associations: jpackage ignores --file-associations when it
        // packages an existing app image (as the beryx plugin does), so our
        // own postinst/prerm register the mime types.
        // Not filtered: they are shell scripts, full of $.
        from("src/main/packaging/linux/postinst")
        from("src/main/packaging/linux/prerm")
        into("build/prepackage")
    }

    // The files used by postinst go into the app image, in /opt/jsesh/lib once installed.
    tasks.named("jpackageImage") {
        doLast {
            val libFolder = file("build/jpackage/${fullName}/lib")
            if (libFolder.exists()) {
                copy {
                    from("src/main/packaging/linux/jsesh-mime.xml")
                    from("src/main/packaging/linux/iconeTextMdC.png") {
                        rename { "jsesh-document.png" }
                    }
                    into(libFolder)
                }
            } else {
                println("Problem : app folder not built")
            }
        }
    }
}


tasks.named("jpackageImage") {
    dependsOn("copyResources")
}


runtime {
    options.set(listOf("--strip-debug", "--no-header-files", "--no-man-pages"))
    modules.set(listOf("java.desktop", "java.logging"))



    jpackage {
        appVersion = project.version.toString().replace("-SNAPSHOT", "") // Replace optional "-SNAPSHOT" suffix for jpackage
        imageName = "${applicationName}-${project.version}"
        installerName = "${applicationName}"
        resourceDir = file("build/prepackage")
        jvmArgs = listOf(
            "-Xmx2G",
        )
        // Gives  information about the build
        installerOptions.addAll(listOf(
            "--verbose"
        ))


        val type = project.findProperty("installerType") as String?
        if (type != null) {
            installerType = type
        }

        installerOptions.addAll(
            listOf(
                "--verbose",
                "--copyright", "Serge Rosmorduc, CeCILL-C license"
            )
        )

        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            // The installer is built by jpackageWindowsInstaller (see above):
            // here, we only want the app image.
            skipInstaller = true
        } else if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            // On Linux, our default installer type is dep
            if (installerType == null) {
                installerType = "deb"
            }
            // No --file-associations here: ignored with an app image.
            // See postinst, jsesh-mime.xml and JSesh.desktop (MimeType, %F).
            installerOptions.addAll(listOf(
                "--linux-shortcut"
            ))
        } else if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
            // On MacOS, our default installer type is dmg
            if (installerType == null) {
                installerType = "dmg"
            }
            
            installerOptions.addAll(listOf(
                "--mac-package-identifier", "${project.group}.${applicationName}".lowercase(),
                "--mac-package-name", "${applicationName}",
                "--file-associations", "jsesh-file-association.properties"             
            ))

            if (installerType == "dmg") {
                installerOptions.addAll(listOf(
                    // "--file-associations", "${projectDir}/src/main/packaging/mac/jsesh-file-association.properties"             

                    //"--icon", "${projectDir}/src/main/packaging/hibou.icns",

                    // "--mac-dmg-content", "${projectDir}/src/main/dist/documentation.md",
                    "--mac-dmg-content", "src/main/binary/licenses",
                    "--mac-dmg-content", "src/main/binary/FONT-LICENSE.md",
                    "--mac-dmg-content", "src/main/binary/LICENSE.txt",
                    "--mac-dmg-content", "src/main/binary/Readme.html"

                ))
            }
        }
    }
}

