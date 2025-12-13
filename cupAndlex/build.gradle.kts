description = "Java CUP and JLex"

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

// Exclude Maven MOJOs
sourceSets {
    main {
        java {
            exclude("org/qenherkhopeshef/jsesh/prepare")
        }
    }
}
