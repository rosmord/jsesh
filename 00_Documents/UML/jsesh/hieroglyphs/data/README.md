# UML Analysis of package `jsesh.hieroglyphs.data`

Current version :

~~~plantuml
@startuml
skin rose

package jsesh {

    package jsesh.mdc.model {
        class TopItemList
    }

    
    package jsesh.hieroglyphs {
        package jsesh.hieroglyphs.data {
            class GardinerCode {
                getFamily(): String
                getNumber(): int
                getVariantPart(): String
                getUserId(): int
                {static} createGardinerCode(String code); 
                {static} compareCode(c1,c2);
                {static} getGardinerFamilies();
                {static} getCodeComparator(); 
                {static} isCanonicalCode(String); 
                {static} isCorrectGardinerCode(String); 
                {static} isCorrectGardinerCodeIgnoreCase(String); 
                {static} getCodeForFileName(String); 

            }

            note top of GardinerCode: Structured representation of a Gardiner Code.

            interface HieroglyphCodesSource {
                hasCode(String code): boolean
                getCodes(): Set<String>
            }

            class ManuelDeCodage <<singleton>> {
                {static} getInstance()
                getTallNarrowSigns(): List<String>
                getLowBroadSigns(): List<String>
                getLowNarrowSigns(): List<String>
                getCanonicalCode(String): String
                isKnownCode(String): boolean
                getBasicGardinerCodesForFamily(): List<String>
            }

            class HieroglyphDatabaseFactory <<helper>> {
                getUserSignDefinitionFile(): File
                - buildInstance(HieroglyphCodesSource): SimpleHieroglyphDatabase
            }

            note bottom of HieroglyphDatabaseFactory: possibly deactivated in the current dev. branch

            SimpleHieroglyphDatabase <. HieroglyphDatabaseFactory

            interface HieroglyphDatabaseInterface {
                getCanonicalCode(code: String): String
                getCodesForFamily(family: String, includeVariants: boolean)
                getCodesSet(): Set<String> 
                getDescriptionFor(String code): String 
                getFamilies(): List<HieroglyphFamily> 
                getPossibilityFor(String phoneticValue, String level): PossibilitiesList 
                getSignsContaining(String code): Collection<String> 
                getSignsIn(String code): Collection<String> 
                getSignsWithTagInFamily(String currentTag, String familyName): Collection<String> 
                getSignsWithoutTagInFamily(String familyName): Collection<String> 
                getTagsForFamily(String familyName): Collection<String> 
                getTagsForSign(String gardinerCode): Collection<String> 
                getValuesFor(String gardinerCode): List<String> 
                getVariants(String code): Collection<SignVariant> 
                getVariants(String code, VariantTypeForSearches): Collection<String>
                getTransitiveVariants(String code, VariantTypeForSearches): Collection<String>
                isAlwaysDisplayed(String string): boolean 
                getCodesStartingWith(String code): PossibilitiesList
                getSuitableSignsForCode(String code): PossibilitiesList
            }

            HieroglyphDatabaseInterface ..> HieroglyphFamily
            HieroglyphDatabaseInterface ..> PossibilitiesList
            HieroglyphDatabaseInterface ..> SignVariant
            HieroglyphDatabaseInterface ..> VariantTypeForSearches
            enum VariantTypeForSearches {}

            class HieroglyphFamily {
                getCode(): String
                getDescription(): String    
            }

            class MultiLingualLabel {
                getLabel(lang): String
                getDefaultLabel(): String
                getLabelKey(): String
                getAllTranslations(): TranslationInfo[]
            }

            MultiLingualLabel ..> TranslationInfo

            class TranslationInfo {
                lang: String
                label: String
            }


            class PossibilitiesList {
                getPhonCode(): String
                next()
                getCurrentSign(): Possibility
                isEmpty(): boolean
                asList(): List<Possibility>
                add(PossibilitiesList): PossibilitiesList    
            }

            class Possibility {
                code : String
                topItemList: TopItemList
            }


            PossibilitiesList ..> Possibility
            Possibility .up.> jsesh.mdc.model.TopItemList

            class SignDescriptionConstants {
                // various Strings
            }

            class SignInfo {
                getCode(): String
                getDescription(): String
                getDeterminativeValueSet(): Set<String>
                getTagSet(): Set<String>
                getSignsContainingThisOne(): Set<String>
                getTranslitterationList(): List<StringTranslitteration>
                getVariants(): Set<SignVariants>
                getSubSigns(): Set<String>
            }

            SignInfo ..> SignTransliteration

            class SignTransliteration {
                translitteration: String
                use: String
                type: SignValueType
            }

            SignTransliteration -> SignValueType

            enum SignValueType {}

            class SignVariant {
                code: String
                type: SignVariantType
            }

            enum SignVariantType {}
            
            SignVariant ..> SignVariantType


            class SimpleHieroglyphDatabase  {}

            SimpleHieroglyphDatabase ..> HieroglyphCodesSource
            HieroglyphDatabaseInterface <|.. SimpleHieroglyphDatabase
            SimpleHieroglyphDatabase ..> SignInfo
            SimpleHieroglyphDatabase ..> MultiLingualLabel
            SimpleHieroglyphDatabase ..> ManuelDeCodage
        }



        package jsesh.hieroglyphs.graphics {
            interface HieroglyphicFontManager {
                hasCode(String): boolean
                get(String): ShapeChar
                getSmallBody(String): ShapeChar
                hasNewSigns(): boolean
            }

            HieroglyphicFontManager .> ShapeChar


            class CompositeHieroglyphicFontManager {
                addHieroglyphicFontManager(HieroglyphicFontManager)

            }

            HieroglyphicFontManager <|.. CompositeHieroglyphicFontManager
            HieroglyphicFontManager "*" <--- CompositeHieroglyphicFontManager 


            class DefaultHieroglyphicFontManager <<singleton>> implements HieroglyphicFontManager {

            }

            DefaultHieroglyphicFontManager --up> CompositeHieroglyphicFontManager
            DefaultHieroglyphicFontManager -up> DirectoryHieroglyphicFontManager
            DefaultHieroglyphicFontManager -up> ResourcesHieroglyphicFontManager
            DefaultHieroglyphicFontManager -up> MemoryHieroglyphicFontManager


            

            class DirectoryHieroglyphicFontManager {

            }

            HieroglyphicFontManager <|.. DirectoryHieroglyphicFontManager 

            class ResourcesHieroglyphicFontManager implements HieroglyphicFontManager {

            }

            class HieroglyphPictureBuilder {
                size: int
                maxSize: double
                setComponent(Component)
                createHieroglyphIcon(code, size, border, component) : Icon
            }

            HieroglyphicFontManager <. HieroglyphPictureBuilder 

            
            class LigatureZone {
                box: Rectangle2D
                verticalGravity: VerticalGravity
                horizontalGravity: HorizontalGravity
            }

            class HorizontalGravity {}
            class VerticalGravity {}

            LigatureZone ..> HorizontalGravity
            LigatureZone ..> VerticalGravity

            class LigatureZoneBuilder {
                LigatureZoneBuilder(ShapeChar)
                getLigatureArea(int)
            }

            ShapeChar <.. LigatureZoneBuilder
            LigatureZone <.. LigatureZoneBuilder


            class MemoryHieroglyphicFontManager <<singleton>> {
                - BzrSimpleFont fonts[]

            }

            HieroglyphicFontManager <|.. MemoryHieroglyphicFontManager 

            class ShapeChar {
                getBBox()
                getShape()
                draw()
                getTransformedShape()
                getSignArea()
                scaleToHeight()
                scaleGlyph()
                exportToSVG()
                writeSVGPath()
                getZone()
                getAuthor()
                getDocumentation()
                getLicense()
            }

            LigatureZone "zones" <-- ShapeChar 

            together {
                class DirectoryHieroglyphicFontManager
                class MemoryHieroglyphicFontManager
                class DefaultHieroglyphicFontManager
                class CompositeHieroglyphicFontManager
                class ResourcesHieroglyphicFontManager
            }
        }

    }

    jsesh.hieroglyphs.graphics.HieroglyphicFontManager .up...|> jsesh.hieroglyphs.data.HieroglyphCodesSource
    
}
@enduml
~~~

## Problems

- the dependency to `TopItemList` induces a circular dependency between packages.
- for `SignTransliteration`, `use` should have an `enum` value (`KEYBOARD`, `PALETTE` or `INFORMATIVE`)
- Where to put the static methods of `GardinerCode` ?
- replace « `Directory` » by « `Folder` »  in names (e.g. `FolderHieroglyphicFontManager` instead of `DirectoryHieroglyphicFontManager`)
- replace HorizontalGravity and VerticalGravity by enums
- The `data` package covers two concerns : representing signs codes **and** the internal, gui-related system of `PossibilityLists`. Those might be separated.

## Solutions

- move the generation of PossibilityLists out of `jsesh.hieroglyphs.data` ; 
- isolate the classes to design a clean interface with facades ;
- yet ensure customization is possible ;
- move `HieroglyphCodesSource` to a `jsesh.hieroglyphs.common` package in order to remove dependency between Graphics and packages ;


