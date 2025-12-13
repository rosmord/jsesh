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