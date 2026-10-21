/// # A very simple framework for Graphical Users Interfaces
///
/// The classes in this package provide a relatively easy way to program users
/// interfaces, to generate localized menus and actions from configuration
/// files, and to create an interface where things like action activation, or
/// radio button values can be linked in a reasonably clean way to an abstract
/// interface.
///
/// Note that those classes can be used alongside more complex frameworks, like
/// Werner Randelshofer's applicative framework in JHotdraw 7.
///
/// The framework is based on an article by Hans Muller on java.net. We have
/// decided to create yet one more framework because we wanted a very light
/// one, as it looked like that sooner or later Java would include a "standard"
/// framework. In particular, we did want a standalone system, which would not
/// rely on third party software.
///
/// We first created a rather specific framework for JSesh, and then we decided
/// to generalize it, introducing hooks if necessary to suppress dependencies.
///
/// The whole content of jsesh.uiFramework is covered by the BSD license, so
/// you have maximal freedom with it. The system currently provides:
///
/// - A simple way to create and configure actions and to link them to
///   particular methods, with many goodies (for instance, it's possible to
///   associate an action with a particular method call, with arguments).
/// - A way to link action status to simple boolean methods.
/// - A very simple way to create menus.
/// - The action system can be used at application level, but it's also usable
///   for custom components.
///
/// All is done through simple text files (in UTF-8). No XML is required.
///
/// For a complete (and mac-friendly) applicative framework, have a look at
/// JHotdraw. Netbeans or Eclipse frameworks are very powerful, and provide
/// pluggable applications, but are really not native-ui frameworks (specially
/// on the Mac).
///
/// # General architecture of the system
///
/// The main element of the system is the BundleAction, a kind of action which
/// takes most of its properties from property files, which can be
/// multilingual. Lots of elements can be set in those files.
///
/// Among those properties are a method name and optionally parameters. When
/// the action is created, it is given a "target" object and the method name is
/// set. Thus, in this system, all actions will call method on the same object,
/// which will be a kind of facade. Actually, it is possible (and relatively
/// easy) to have multiple facades.
///
/// The properties which define the action have the form:
///
/// ```
/// actionID.PropertyName=....
/// ```
///
/// where actionID is the identifier of the action. In fact, it's simply the
/// name of the method to call on the target. If the actionID contains an
/// underscore, the text after the underscore will be understood as a parameter
/// for the method, and passed as a String.
///
/// ```
/// display_hello.Name=Ave
/// ```
///
/// defines the Name (in fact, the menu or button label) of the action
/// display_hello, which will call the method `display(String)` and pass it the
/// string "hello" when actionPerformed is called.
///
/// ## Simple scenario
///
/// 1. Load the property files by creating an AppDefaults object.
/// 2. Create the actions with an action factory. The list of actions to create
///    is normally stored in a text file, loaded as a resource. In simple case,
///    you can use the file which describes your menus.
/// 3. Create the menu from the actions and a menu description, which is a
///    rather simple text file, with the action ID laid out to look like a menu.
///
/// ## Possible variation
///
/// If one wants to have "real" actions, it is possible. One can create a class
/// which extends ActionFactory, and redefine the createAction method.
///
/// ## Action redirection
///
/// ## Dev. Comment (current ongoing development)
///
/// Each document window has its own menus. Hence, most action are really
/// document-actions, not application actions. Note that, as document actions
/// are created for each document, they should be reasonably lightweight, on
/// one hand, and created on-the-fly. Which means that an ActionFactory might
/// be called on a living application.
package org.qenherkhopeshef.guiFramework;
