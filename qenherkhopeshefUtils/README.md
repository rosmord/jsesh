# Qenherkhopeshef Utils

Various reusable software packages I use, first in JSesh and then in other projects. 

## `org.qenherkhopeshef.algo`

Generic data structures. Currently:

`ReversibleMultiHashMap`
: a *n-to-m* map, which I use to link multiple views to multiple model elements. Of cours, it can be used for any purpose.

## `org.qenherkhopeshef.beans`

`MutableReference`
: an observable bean holding an object.

## `org.qenherkhopeshef.guiFramework`

A first attempt at a generic GUI framework, which we abandonned in favor of Werner Randelshofer's `jhotdraw`. It allows one to define menus through text files. The sub-folders contain other GUI-related utilities, which might be useful without the framework.

### `org.qenherkhopeshef.guiFramework.busy`

Defines a `BusyAdvanceGlassPane` which can be used to block an UI while a long-running task is performed.

### `org.qenherkhopeshef.guiFramework.demo`

A basic demo of the GUI framework.

### `org.qenherkhopeshef.guiFramework.splash`

A splash screen implementation (not very useful since Java has a standard one). Used by JSesh at startup.

### `org.qenherkhopeshef.guiFramework.swingworker`

Simplified versions of `SwingWorker`.

## `org.qenherkhopeshef.json`

Generic JSON representation. I'm not even sure I have ever used it really.

## `org.qenherkhopeshef.properties`

A system of mutable and observable properties. Probably never used as well.

## `org.qenherkhopeshef.swingUtils`

Generic usable small utilities for Swing.

errorHandler
: package to provide a simple way to display error messages to the end-user (especially for bug reports).

`org.qenherkhopeshef.swingUtils.lists`
: Utilities for `JLists`and `JComboboxes`.

`org.qenherkhopeshef.swingUtils.portableFileDialog`
: A front-end for file choosers, hiding both the old `FileDialog` and the new `JFileChooser` behind a common interface. I guess that `FlatLaf` has probably made this obsolete.

`DoubleDimension`
: a `Dimension` with double precision;

`GraphicsUtils`
: mainly provides a simple `antialias` method;

`LabelDelegateListCellRenderer`
: a `ListCellRenderer` which allow to compute the label of an entry with a specific method.

`PortableToolKit`
: a facade hiding the GUI toolkit, so that headless mode can be used.

## `org.qenherkhopeshef.utils`

Generic utilities:

`PlatformDetection`
: returns the platform currently used (Windows, MacOS, Linux).

`StringUtils`
: generic string utilities.

## `org.qenherkhopeshef.viewtoolkit1`

A generic toolkit for creating complex views, with examples. It's used in the editor of syntactic analysis of the Ramses project.

The folder contains three demo projects.




