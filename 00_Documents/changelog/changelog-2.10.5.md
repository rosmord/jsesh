*Released 2009/04/29*

* new families from S. Thomas : P, Q, R and S families are now complete
* use of the Quaqua library on the mac : better look and feel (and, at last, a sensible folder selector).
* lots of work done for the IFAO :
    * PDF copy/paste now available on the Mac. As far as I know, works with Word 2008 but *not* with Word 2004, nor with Openoffice/NeoOffice.
    * Wysiwyg PDF option for PDF file save.
       Note that currently, the available layout options are a bit limited. The  JSesh layout algorithm has 
       no idea of the page size, and hence the result might not be what you want. I'll fix this, but meanwhile 
       I would suggest using SVG when a fine and controlled output is needed.
    * The PDF files use correct CMYK black colour (important for printing workshop)
    * Document preferences are now saved: line, column, small signs centered...
    * cartouche line width is also saved as a software option.
    * JSesh can use a separate set of signs for small glyphs drawn in a small scale
    * shading symbols are available as signs from the menus (they were understood by JSesh from day one, but I didn't think it was interesting to use them. However, If used in conjunction with groups, they allow specific shading to be edited.
    * Square brackets and shading signs are now selectable in the group editor (note that in the future, I indend bracket to scale automatically according to their surrounding, and I will also add sign shading).
    * new drawings for ecdotic marks
