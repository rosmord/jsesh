description = """
    create a zip file containing the jsesh document templates, in the app path .resources.jsesh-templates.zip
    """

plugins {
    id("jsesh.java-conventions")
}

dependencies {
    // Only to check that the bundled templates are correct JSesh documents.
    testImplementation(project(":jsesh"))
}

val generatedResourcesDir = layout.buildDirectory.dir("generated/resources")

val zipTemplates = tasks.register<Zip>("zipTemplates") {
    archiveFileName.set("jsesh-templates.zip")
    destinationDirectory.set(generatedResourcesDir.map { it.dir("jsesh/jseshtemplates") })
    from("src/main/assets")
}

sourceSets {
    main {
        resources.srcDir(generatedResourcesDir)
    }
}

tasks.named("processResources") {
    dependsOn(zipTemplates)
}
