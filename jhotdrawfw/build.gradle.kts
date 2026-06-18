description = "A slightly modified version of Werner Randelshofer's Jhotdraw application framework (jhotdraw 7.3)."

plugins {
    id("jsesh.java-conventions")
}

// JHotDraw stores .properties files alongside Java source; include them as resources.
tasks.processResources {
    from("src/main/java") {
        include("**/*.properties")
    }
}
