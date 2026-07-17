description = """
    Generic i18n texts, usable by all JSesh components.
        
    The final idea, even if it's not very modular, will be to put all 
    labels here, in order to ease editing.
    
    Also, the current libraries will use those files, but in a first step,
    without breaking them, even if we use at that time two or three different 
    systems.
""".trimIndent()

plugins {
    id("jsesh.java-conventions")
}

// Rewrites the localized label files so that they mirror labels.properties.
// Run on demand: ./gradlew :jseshLabels:update-labels
tasks.register<org.qenherkhopeshef.jsesh.gradle.UpdateLabelsTask>("update-labels") {
    group = "labels"
    description = "Rewrites the localized label files to mirror the structure of labels.properties."
    labelsDirectory.set(layout.projectDirectory.dir("src/main/resources/jsesh/resources"))
    referenceFile.set(labelsDirectory.file("labels.properties"))
}
