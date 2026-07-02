description = """
    Installer for JSesh, using jpackage.

    The idea is to generate a native installer, and even to have github generate it for us.

    In practice, it demands a lot of care depending on the target platform.

    See also https://docs.oracle.com/en/java/javase/25/jpackage/index.html

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


// Windows MSI installer

if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
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
}

// MAC OS X Installer
// PATCH !!!!
// We might need to delete part of this later.
// The point here is to ensure the icons are copied into the app.
if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
    val copyResources = tasks.register<Copy>("copyResources") {
        from("src/main/packaging/mac/hibou.icns") {
            rename { "${applicationName}-${project.version}.icns" }
        }
        into("build/prepackage")
        doLast {
            println("Copied resources to build/prepackage")
        }   
    }

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
}

if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
    val copyResources = tasks.register<Copy>("copyResources") {
        from("src/main/packaging/linux")//  { filter { line: String -> line.replace("\${project.version}", project.version.toString()) } }    
        into("build/prepackage")                
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

        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            // Default installer type is msi
            if (installerType == null) {
                installerType = "msi"
            }
            installerOptions.addAll(listOf(
                //"--win-per-user-install", would install in C:\Users\user-name\AppData\Local\application-name
                "--win-dir-chooser",
                "--win-menu",
                "--win-shortcut"                
            ))            
        } else if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
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
                    //"--mac-dmg-content", "${projectDir}/src/main/dist/data"
                ))
            }
        }
    }
}

