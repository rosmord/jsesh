description = "A slightly modified version of Werner Randelshofer's Jhotdraw application framework (jhotdraw 7.3)."

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

// Configure resources to include .properties files from src/main/java
tasks.processResources {
    from("src/main/java") {
        include("**/*.properties")
    }
}
