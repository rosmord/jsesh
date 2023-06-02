# MVC Management in JSesh (working document)

In JSesh, the main component, which displays hieroglyphic texts depends on three types of data :

- the hieroglyphic text itself ;
- the document layout information ;
- various other preferences (fonts, for instance).

Currently, the last two parts are not very well delimited.

Moreover, the rest of the software is not fully *reactive*. For instance, when changing the content of the sign description files, those are not taken into account immediately.

The MVC part for the hieroglyphic text is relatively classical. It might be improved (we have started an experimental system to do so, in the line of code we have written for Ramses), but it's relatively ok.

For the rest of the code, which did not interest us as much when we wrote it, things are not as clear.

One problem is that we would like, at the same time :

- an elegant MVC approach ;
- the possibility to share properties between elements if we need to

The main problem would be for instance to :

- react to preferences modifications ;
- be able to share preferences as we like ;
- and even, to be able to link the preferences used by a certain dialog to « the preferences of the selected window » or something like that.
- if possible, be independant of 3rd party libraries, except if they are somehow a standard (which, given the state of Java for desktop, is somehow unlikely).




## Study of various systems

### How things are done in VueJS

In `VueJS`, we have the following systems/types :

- `ref()` : builds a reference to a standard object ;
- `shallowRef()` non-deeply reactive reference ; can be controlled with `triggerRef`
- `computed()` depends on other objects, and is modified when they are ;
- `watchEffect()` : effet de bord réactif (relancé à chaque modification de ses dépendances) ;
- `reactive()` : crée une version réactive de l'argument.

### How things are done in JavaFX

We have **properties**. They can wrap a *value* ; and can be bound.

See [the doc](https://docs.oracle.com/javafx/2/binding/jfxpub-binding.htm) for an example of reactive expression.
See also [this wiki](https://wiki.openjdk.org/display/OpenJFX/JavaFX+Property+Architecture)
and [this](https://blog.joda.org/2007/10/java-7-properties-terminology_6780.html)

- `ObservableValue` wraps a value and allows to observe the value for changes ; it *can* be lazyly computed when `getValue()` is called, 
- `Properties` which can hold *settable* values.

### How things are done in SweetHome 3D

In sweet home 3D, preferences are standard Java beans, using `propertyChangeSupport` to manage them.
The preference object is passed to the *actions* when they are built (hence, it's always a shared preference object).
This would not be usable for JSesh, as JSesh editor can be either used in JSesh itself (which tends to share preferences), and 
in other softwares (which might differ).

### RxJava et al.

Found this [interesting blog](https://github.com/Petikoch/Java_MVVM_with_Swing_and_RxJava_Examples) about using RxJava for Swing apps.

- lots of interesting [ideas about GUI and reactive ](https://continuously.dev/blog/2015/02/10/val-a-better-observablevalue.html)

## The issue for Preferences

Apart from the basic model we use, we would like to be able to react to many changes in the data.

For instance, when properties are edited, we would like to adapt everything which depends on them.

Regarding preferences, we would like to be :

- able to share them
- able to keep them apart
- able to react when they are modified.

Having some kind of ObjectProperty, which might be shared (or preferenceHandler, or what you call it), would do.
It should be observable, at least for simple, global change events, or the value could even be immutable.

After giving it some though, it seems that a simple way would be :

- to have a setPreference method to set the preference object ; a call to this method will cause a recomputation of most view data ;
- to make preferences observable, so that objects which listen to preferences will get notified and react accordingly.

I've got the feeling that it's more or less the case now, BTW.

The centralized preferences for JSesh could be managed in the following way :

- A PreferenceSingleton class would be usable ;
- Methods should be provided :
  - to initialize this singleton from JSesh defaults
  - to initialize it from user-supplied defaults
  - it should have reasonnable defaults *in-memory* anyway

