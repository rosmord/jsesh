plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

dependencies {
    api(project(":jsesh"))
    api(project(":jseshGlyphs"))
    api(project(":jhotdrawfw"))
    api(project(":jseshSearch"))
    api(project(":qenherkhopeshefUtils"))
    api("com.miglayout:miglayout:3.7.4")
}
