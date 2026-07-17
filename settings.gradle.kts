plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "jsesh-all"

include("cupAndlex")
include("cupruntime")
include("qenherkhopeshefUtils")
include("jsesh")
include("jsesh-installer")
include("jseshGlyphs")
include("jhotdrawfw")
include("jseshSearch")
include("jseshAppli")
include("signInfoAppli")
include("jseshLabels")
include("jseshTests")
include("jsesh-appli-installer")

