---
title: "Audit of resources"
date: 2026-07-09
---

# Full diagram of resources

~~~plantuml
@startuml resources
skin rose
hide empty members

' Updated 2026-07-09 from the original 00_Documents/audit/resources.puml
' (2025-05-13), checked against the current source tree. Renames since then:
'   JSeshGlossary -> Glossary
'   HieroglyphDatabaseInterface -> HieroglyphDatabase
' plus the introduction of CanonicalCode, PossibilityRepository becoming a
' real class, and a small observer mechanism on HieroglyphShapeRepository.
' The refactoring proposed in 20250709-resources_creation.md
' (JSeshCompendiumLoader / HieroglyphCompendium) has NOT been implemented.

class JSeshRenderContext <<record>> {
    jseshStyle : JSeshStyle
    hieroglyphShapeRepository: HieroglyphShapeRepository
}

JSeshRenderContext ..> HieroglyphShapeRepository
JSeshRenderContext ..> JSeshStyle

class JSeshStyle <<record>> {}

interface CanonicalCode {}

interface HieroglyphCodesSource {
    hasCode(code : CanonicalCode): boolean
    getCodes() : Set<String>
}

HieroglyphCodesSource ..> CanonicalCode

interface HieroglyphShapeRepository extends HieroglyphCodesSource{
    get(CanonicalCode code): ShapeChar
    getSmallBody(CanonicalCode code): ShapeChar
    hasNewSigns(): boolean
    {static} getStandardShapeRepository(): HieroglyphShapeRepository
    addListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent>)
    removeListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent>)
}

HieroglyphShapeRepository ..> ShapeChar
HieroglyphShapeRepository ..> CanonicalCode
HieroglyphShapeRepository ..> HieroglyphShapeRepositoryChangedEvent

class HieroglyphShapeRepositoryChangedEvent {}

class ShapeChar {}

class StandardFontShapeRepository <<package local>> implements HieroglyphShapeRepository {}

class DirectoryHieroglyphShapeRepository implements HieroglyphShapeRepository {}

interface HieroglyphToolkit {
    {static} standardHieroglyphToolKit() : SimpleHieroglyphToolkit

    hieroglyphShapeRepository() : HieroglyphShapeRepository
    possibilityRepository() : PossibilityRepository
    hieroglyphDatabase() : HieroglyphDatabase
}


HieroglyphToolkit ..> HieroglyphDatabase

HieroglyphToolkit ..> PossibilityRepository

class PossibilityRepository {
    PossibilityRepository(HieroglyphDatabase, Glossary)
    getPossibilityListFor(String code) : PossibilitiesList
    update(EventObject ev)
}

PossibilityRepository ..> HieroglyphDatabase
PossibilityRepository ..> Glossary

class JSeshUserSignLibraryConfiguration implements HieroglyphToolkit{
    glossaryManager() : GlossaryManager
}

class SimpleHieroglyphToolkit implements HieroglyphToolkit {
    {static}  embeddedOnlyInstance() : HieroglyphToolkit
    {static} buildFontWithoutUserDefinitions  (HieroglyphShapeRepository font,
     Glossary glossary) : SimpleHieroglyphToolkit
    {static} buildFontWithUserDescriptions(HieroglyphShapeRepository fonts,
            Glossary glossary)
    SimpleHieroglyphToolkit(HieroglyphShapeRepository hieroglyphShapeRepository,
            PossibilityRepository possibilityRepository, HieroglyphDatabase hieroglyphDatabase)
}

class PredefinedFonts <<utility>> {
    {static}standardJSeshFont() :  HieroglyphShapeRepository
    {static}  gnuTraceFont() : HieroglyphShapeRepository
    {static}  compositeFont() : CompositeHieroglyphShapeRepository
}

PredefinedFonts ..> CompositeHieroglyphShapeRepository

class CompositeHieroglyphShapeRepository implements HieroglyphShapeRepository {
    addHieroglyphicFontManager(HieroglyphShapeRepository repository)
}


interface HieroglyphDatabase {
    getPossibilityFor(String phoneticValue, String level): PossibilitiesList
    getValuesFor(String gardinerCode) : List<String>
    ' ~15 further query methods (families, tags, variants...) omitted for clarity
}

class HieroglyphDatabaseFactory <<utility>> {
    {static} buildWithUserDefinitions(HieroglyphCodesSource) : HieroglyphDatabase
    {static} buildPlainDefault(HieroglyphCodesSource) : HieroglyphDatabase
    {static} getUserSignDefinitionFile() : File
}

class DefaultHieroglyphDatabase implements HieroglyphDatabase {}

HieroglyphDatabaseFactory ..> DefaultHieroglyphDatabase
HieroglyphDatabaseFactory ..> HieroglyphCodesSource

class GlossaryManager {
    getGlossary(): Glossary
}

class Glossary {

}

class GlossaryEntry {}

Glossary --> "*" GlossaryEntry

class JMDCEditor {
    JMDCEditor(HieroglyphicTextModel, JSeshStyle, HieroglyphToolkit)
    JMDCEditor(HieroglyphicTextModel, JSeshStyleReference, HieroglyphToolkit)
}

JMDCEditor ..> JSeshStyle
JMDCEditor ..> JSeshStyleReference
JMDCEditor ..> HieroglyphToolkit
JMDCEditor ..> HieroglyphicTextModel

class JSeshStyleReference {}

JSeshStyleReference ..> JSeshStyle

class HieroglyphicTextModel {}

JSeshUserSignLibraryConfiguration ..> GlossaryManager
JSeshUserSignLibraryConfiguration ..> HieroglyphDatabaseFactory

SimpleHieroglyphToolkit ..> HieroglyphDatabaseFactory

class JSeshFullHieroglyphShapeRepository implements HieroglyphShapeRepository  {
    setDirectory(File directory)
}

JSeshFullHieroglyphShapeRepository ..> DirectoryHieroglyphShapeRepository
JSeshFullHieroglyphShapeRepository ..> CompositeHieroglyphShapeRepository

GlossaryManager -> Glossary

PredefinedFonts ..>  HieroglyphShapeRepository
@enduml
~~~