# How to build resources

Regarding the available resources and preferences, here is a documentation about how we can access them.

- *Note that a revision of this system could be useful, in order to make is more cohesive*.
- *names are liable to change*.


The current philosophy is:

- immutable values can be accessed through a singleton ;
- other values can be created, but should normally be shared. The best way would be either to use *dependency injection* or to create a single instance of them at the *top level* of the application. Component should not need to decide whence they take their data.

## Defaults (without access to user preferences)

`JSeshStyle`
: A default value is available as a static field in the class, `JSeshStyle.DEFAULT`. Note that the class is immutable.

`HieroglyphShapeRepository`
: the interface has a static method called  `getStandardShapeRepository` which returns a singleton instance of a font based on old Gardiner-like tksesh font and the modern JSesh font (if its jar is available). Fine-grained access is available through the class `PredefinedFonts`, with static methods to create instances of the various fonts. The `HieroglyphShapeRepository.getStandardShapeRepository()` method is more convenient.

`HieroglyphDatabaseInterface`
: a call to `HieroglyphDatabaseFactory.buildPlainDefault(...)` will give you a database with only the JSesh embedded data.

  The following code would do:
  ~~~java
  var shapeRepository = HieroglyphShapeRepository.getStandardShapeRepository()
  var database = HieroglyphDatabaseFactory.buildPlainDefault(shapeRepository);
  ~~~

`JseshFontKit`
: an interface which represents a <strong>coordinated</strong> n-uplet of  `PossibilityRepository`,  `HieroglyphShapeRepository` and `HieroglyphDatabaseInterface`. The class `SimpleFontKit` provides a singleton `embeddedOnlyInstance` and convenient named constructor for more versatile instances.

`JSeshRenderContext`
: used as argument of **drawing operations.**. Can be built on the fly. It's a couple of `HieroglyphShapeRepository` and `JSeshStyle`. It can be built with its very simple constructor.

## With user preferences

The simplest way to access font resources is to use `JSeshUserSignLibraryConfiguration`.

`JSeshUserSignLibraryConfiguration`
: An instance of this class, created by calling its default constructor, is a `JSeshFontKit`. It also gives access to `JSeshFullHieroglyphShapeRepository`, `GlossaryManager`, and `HieroglyphDatabaseInterface`. In most cases, only one instance of this class should be created. That is, it should be a singleton in the **Spring** meaning of the term, even if it's not one in the GoF sense.

`HieroglyphShapeRepository`
: has an implementation which provides access to the user folder, `JSeshFullHieroglyphShapeRepository`. It can be instanciated using its constructor; 

`JseshFontKit`
: use the various named constructors of `SimpleFontKit` to create an instance with the user preferences. The simplest way is to use `JSeshUserSignLibraryConfiguration`.

`Glossary`
: available through the `GlossaryManager`, which is a singleton. It will automatically load the user glossary.


User sign database source
: The XML file which may contain user-defined sign properties is found at `HieroglyphDatabaseFactory.getUserSignDefinitionFile()`.
