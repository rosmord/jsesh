# Copy of the message of [kyaneticblue](https://github.com/kyaneticblue) about Gradle migration



I've had a go at this here and it all seems to be working fine:

https://github.com/kyaneticblue/jsesh/tree/gradle

I left the Maven build intact so either can be used. A few notes:

## LexMojo and CupMojo

I've replaced these with the generateLex and generateCup tasks in the jsesh module. To prevent build issues, the Mojo classes are explicitly excluded in the Gradle build.

## PrepareFontsMojo

I've replaced these with the generateIndex task in the jseshGlyphs module. I rewrote the required logic within the task to avoid depending on the jsesh module. It's a tad long for a build script but probably acceptable enough. On reflection, given that it is now within the task rather than a plugin included via includeBuild, there may be fewer problems that result from the build bootstrapping its build logic, so it may not be so problematic to depend on jsesh after all.

## Filtered resources

The filtered resources require `${project.version}`; however, in Gradle this should just be `${version}`. To keep these files compatible with the Maven build for now I've used a small expandLine function to shim this in the jsesh-installer and jsesh modules.

## Other notes

I branched this from the master branch. I can change this to the development branch if desired. I've tested this on IntelliJ IDEA 2025.2.5 on Windows. If you are interested in incorporating this and want any changes, let me know and I'll be happy to take a look.

𓋹 𓍑 𓋴


## P.S. (included by S. Rosmorduc)

Here is the `jseshGlyphs/build.gradle.kts` file in kyaneticblue's version.

~~~kotlin
import java.util.Locale

description = """
    Font packager for JSesh.
    Packages fonts in resources dir.
""".trimIndent()

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

val generatedResourcesDir = layout.buildDirectory.dir("generated/resources")

val generateIndex by tasks.registering {
    val inputDir = layout.projectDirectory.dir("src/main/resources/jseshGlyphs")
    val outputFile = generatedResourcesDir.map { it.file("jseshGlyphs/list.txt") }

    inputs.files(
        fileTree(inputDir) {
            include("**/*.svg")
        }
    )
    outputs.file(outputFile)

    doLast {
        val jseshCodeRegex = Regex("(US([0-9]+))?([A-Z]|AA|FF|NL|NU)([0-9]+)([A-Z]*)")
        val numericCodeRegex = Regex("[0-9]+")
        val tkseshUserCodeRegex = Regex("UG(\\d+)M(\\d+)N(\\d+)")
        fun getGardinerCodeForFilename(filename: String): String? {
            val code = filename
                .uppercase(Locale.ENGLISH)
                .removeSuffix(".SVG")
            // Special nTrw code
            if (code == "NTRW") return "nTrw"
            // Special nn code
            if (code == "NN") return "nn"
            // JSesh code
            jseshCodeRegex.matchEntire(code)?.let { matchResult ->
                val userId = matchResult.groups[2]?.value?.toInt() ?: 0
                val family = matchResult.groups[3]!!.value.let {
                    when (it) {
                        "AA" -> "Aa"
                        "FF" -> "Ff"
                        else -> it
                    }
                }
                val number = matchResult.groups[4]!!.value.toInt()
                val variantPart = matchResult.groups[5]!!.value.let {
                    when (it) {
                        "H", "V" -> it.lowercase(Locale.ENGLISH)
                        else -> it
                    }
                }
                return if (userId == 0) {
                    "$family$number$variantPart"
                } else {
                    "US$userId$family$number$variantPart"
                }
            }
            // Numeric code
            if (numericCodeRegex.matches(code)) {
                return code
            }
            // TKSesh user code
            if (tkseshUserCodeRegex.matches(code)) {
                return code
            }
            // Invalid code
            return null
        }

        val lines = inputDir.asFile
            .walkTopDown()
            .filter { it.isFile && it.extension.equals("svg", ignoreCase = true) }
            .map { file ->
                val filename = file.name
                val gardinerCode = getGardinerCodeForFilename(filename)
                "${gardinerCode ?: filename.removeSuffix(".SVG")}\t$filename"
            }
            .toList()

        val out = outputFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(lines.joinToString("\n"))
    }
}

sourceSets {
    main {
        resources.srcDir(generatedResourcesDir)
    }
}

tasks.processResources {
    dependsOn(generateIndex)
}
~~~
