description = """
    create a zip file containing the jsesh text library, in the app path .resources.jsesh-texts.zip
    """

plugins {
    id("jsesh.java-conventions")
}

val generatedResourcesDir = layout.buildDirectory.dir("generated/resources")

val zipTexts = tasks.register<Zip>("zipTexts") {
    archiveFileName.set("jsesh-texts.zip")
    destinationDirectory.set(generatedResourcesDir.map { it.dir("jsesh/jseshtexts") })
    from("src/main/assets")
}

sourceSets {
    main {
        resources.srcDir(generatedResourcesDir)
    }
}

tasks.named("processResources") {
    dependsOn(zipTexts)
}

