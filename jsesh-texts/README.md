# The jsesh-texts sub module

This module contains the text library (copied from the [Manuel de Codage Texts for JSesh project](https://github.com/rosmord/MDC-texts)).

The new system for publishing jsesh versions makes it difficult to include a separate folder for JSesh texts in a portable way, so we have decided to include them in the main software, and to have a menu entry to export them to a folder of the user's choice.

## Layout

The raw text files live in `src/main/assets/`. The Gradle build zips that
folder into `jsesh-texts.zip` and adds it as a resource at
`jsesh/jseshtexts/jsesh-texts.zip`, so it ends up packaged inside this
module's jar alongside `JSeshTextExporter`. No manual step is needed —
`./gradlew build` regenerates the zip automatically.

`JSeshTextExporter.exportTexts(Path destination)` reads that bundled zip
and extracts it into the given destination folder.

