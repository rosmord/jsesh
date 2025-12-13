description = "Demonstrations of the uses of JSesh as a library."

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

dependencies {
    api(project(":jsesh"))
    api(project(":qenherkhopeshefUtils"))
    api("com.miglayout:miglayout:3.7.4")
}
