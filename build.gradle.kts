description = "JSesh complete distribution"

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions") apply false
}

allprojects {
    repositories {
        mavenLocal()
        maven {
            url = uri("http://www.qenherkhopeshef.org/maven")
            isAllowInsecureProtocol = true
        }
        mavenCentral()
    }
}
