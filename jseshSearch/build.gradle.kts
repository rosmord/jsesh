description = """
    Advanced search functions for JSesh.
    We separate them from the original JSesh widget, as they require some external libraries.
""".trimIndent()

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

dependencies {
    api(project(":jsesh"))
    api(project(":jseshLabels"))
    api("com.miglayout:miglayout:3.7.4")
    api("org.qenherkhopeshef:finitestate:2.0")
}
