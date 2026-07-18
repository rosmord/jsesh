description = "The JSesh application, using jhotdraw as GUI framework."

plugins {
    id("jsesh.java-conventions")
    application
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshGlyphs"))
    implementation(project(":jhotdrawfw"))
    implementation(project(":jseshSearch"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation(project(":jseshLabels"))
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
}

application {
    mainClass.set("jsesh.jhotdraw.Main")
    applicationName = "JSesh"
    applicationDefaultJvmArgs = listOf("-Xmx2G")
}

if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
    tasks.named<JavaExec>("run") {
        jvmArgs("-Xdock:name=${application.applicationName}", "-Dapple.laf.useScreenMenuBar=true")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}
