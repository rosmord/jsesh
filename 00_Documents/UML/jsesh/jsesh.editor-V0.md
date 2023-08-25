# UML Analysis of package `jsesh.hieroglyphs.editor` and related classes

~~~plantuml
@startuml

package jsesh.editor {
    skin rose
    class JMDCEditor {
        caretChanged : boolean
        editable : boolean
    }

    interface JMDCModelEditionListener {}

    JMDCEditor ..> DrawingSpecification
    JMDCEditor ..> JMDCModelEditionListener
    JMDCEditor ..> MDCEditorEventsListener
    JMDCEditor -> ViewDrawer
    JMDCEditor ..> ViewBuilder
    JMDCEditor ..> "documentView" MDCView
    JMDCEditor ..> MDCViewUpdater
    JMDCEditor ..> JMDCEditorWorkflow
    JMDCEditor ..> MDCModelTransferableBroker

    DrawingSpecification --|> DrawingPreferences

    interface DrawingSpecification {
        getHieroglyphsDrawer()
        copy()
        extractDocumentPreferences();
        applyDocumentPreferences(prefs);        
    } 

    note bottom of JMDCModelEditionListener : keyboard/mouse events

    class JMDCEditorWorkflow {
        currentCode: StringBuffer
        readWrite: boolean
        insertElement(ModelElement)
        undo()
        redo()
        buildAbsoluteGroup()
    }

    note left of JMDCEditorWorkflow::buildAbsoluteGroup
        Problematic as written : the EditorWorkflows is agnostic regarding
        placement, but <b>buildAbsoluteGroup</b> needs to use
        <b>DrawingSpecifications</b>
    end note

    Observer <|.. JMDCEditorWorkflow
    MDCCaretChangeListener <|.. JMDCEditorWorkflow

    JMDCEditorWorkflow ..> PossibilitiesHandler
    JMDCEditorWorkflow ..> MDCCaret


    JMDCEditorWorkflow ..>  HieroglyphicTextModel
    JMDCEditorWorkflow ..> "*" MDCModelEditionListener : listeners >

    class HieroglyphicTextModel {
        philologyIsSign: boolean
        undoManager: UndoManager
        undo()
        redo()
        mustSave()
    }




    HieroglyphicTextModel ..|> Observable
    HieroglyphicTextModel ..|> ModelElementObserver

    HieroglyphicTextModel -> TopItemList

    class PossibilitiesHandler {
        separator : char        
    }

    PossibilitiesHandler ..> PossibilitiesList 


}


@enduml
~~~

- `DrawingSpecification` vs `DrawingPreferences` : basically, `DrawingPreferences` corresponds to simple values, whereas `DrawingSpecification` holds stuff which can be computed from those values, plus sundry secondary data.
- currently, `DrawingSpecification` is not observable.
- `MDCViewUpdater` should probably hold the view, not the main object ?


