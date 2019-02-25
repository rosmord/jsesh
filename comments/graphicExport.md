# State of the `jsesh.graphics.export`
As of 2019/02/25.

## Current state


### bitmaps

- A very heavy class, BitmapExporter, with ui and embedded classes for the actual export. 
  The embedded classes extend `BaseGraphics2DFactory`
- A class, `BitmapGraphicsFactory`, which is currently **unused**.
 Delete (and perhaps recreate later, but that's what git is for).

### emf

- A class named EMFExporter. Extends AbstractGraphicalExporter, and relatively small.

### eps

- Same as EMFExporter.

### generic

I feel it's quite a mess here.

- BaseGraphics2DFactory : interface for generating graphics 
  which can be saved somewhere ; the name is not very good, as it looks 
  like from it that it's a basic implementation.
- GraphicalExporter : a BaseGraphics2DFactory, with other operations to choose a folder or
  export some MdC data. Should probably **not** extend BaseGraphics2DFactory. 
- AbstractGraphicalExporter : basic implementation of GraphicalExporter
- SelectionExporter : an exporter for exporting selected text to a certain graphics. 
  Uses `BaseGraphics2DFactory`, but not the others.  
- CaretBroker : an object with a caret -- to define a selection
- ExportData : the data needed for an export : the text, selection, and preferences.
- ExportDrawer : base class, used by RTFExporter, to draw *parts* of the text. Look at equivalent in PDF processing.
- ExportOptionPanel : A very generic dialog. With a bit of renaming for the methods,
  this dialog could be reusable -- and moved elsewhere.
  
### html
- HTMLExporter : all-in-one code for gui and logic of HTML export.
- LabeledField : a field with a label - really basic Swing stuff. Used in the HTML preferences
 panel.

## macpict

- MacPictExporter extends AbstractGraphicalExporter ;

relatively small class.

## pdfExport

- JPDFOptionPanel ok, an option panel.
- PDFDataSaver : used by the clipboard; 
   redundant with PDFExporter ; **should use ExportData**
- PDFDocumentWriterAux : an ill-defined class to manage PDF document ; or at least ill-named ; 
 it might be the pdf document itself. its instance is called documentWriter.
- PDFExportConstants : utility class for some constant strings. 
- PDFExporter : the actual pdf exporter. 
  Handles both the logic an the preferences. Should use ExportData. Used in many places
  (export as pdf menu; quick pdf export, and save as pdf)
- PDFExportHelper : utility class.
- PDFExportPreferences : no problem here
- PDFGraphics2DFactory : implements BaseGraphics2DFactory 

## rtf

- JRTFFileExportPreferences : gui panel ;
- RTFExporter : base class for exporting RTF
- RTFExporterPresenter : GUI presenter
- RTFExportGranularity : defines what is grouped as a single picture in the RTF document
- RTFExportGraphicFormat : the embedded graphical formats for RTF (used in selection)
- RTFExportPreferences : choices to make for RTF generation.

## svg

- SVGExporter : extends AbstractGraphicalExporter

## wmf

- WMFExporter extends AbstractGraphicalExporter

## Package newExport 
A new organisation for export. I don't know yet if it's better.