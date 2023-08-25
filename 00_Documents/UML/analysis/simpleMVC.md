# Simple MVC and model architecture for JSesh

This is supposed to be relatively simple implementation of observable properties for JSesh.

- we want properties to be *shareable*
- we want to be able to react to their modification.

For a JSesh document, the view depends on :

- font sources ;
- dimensions and other preferences ;
- the data itself

dimensions and preferences could be dependant on a *view*, on a *document*, or on *the whole application*.

We want, for instance, to be able to :

- have a specific (hieroglyphic) font selection for a given component ;
- have a application-wide font selection ;
- react to change of the font selection source ;
- react to change to the *content* of the font selection change.

We are probably not very interested in *fine grained* information. JSesh is fast enough, and re-computing everything **is** a possibility.

If the data is immutable, we might use something like an observable **ObjectProperty**.

- on a component, **setAProperty(ObjectProperty<A>)** would set the property to use, which might be shared. It's somehow unlikely that this method is called once the application is running.
- the `setValue()` method of the property object could be used to change its value, even though it is shared.
- the use of a property would make explicit the fact that the object can be shared.

- as we use Swing, we should probably stick with the beans and propertyChangeHolder and the like.

To avoid errors, we should use a consistent approach (i.e. only one way of doing things)

- if we go the immutable way for `DrawingSpecifications` :
  1. each object directly holding a `DrawingSpecifications` won't be notified of changes ;
  2. only objects holding a `ObjectProperty<DrawingSpecifications>` will.

It might be annoying if we store an  object which follows pattern (1.), but otherwise is explicit enough.

Mixing this approach with a mutable object would be a mess, as there would be up to three ways of changing the values, and the mutable object would ne


### A few use cases

#### Composite models

#### Shared/Unshared property

Suppose we want :

- a immutable Preference object (but maybe think twice about it. it's not the way we do it at all currently)
- graphic components should be able to share the same preference (and they should all change at the same time)
- a graphic component should be able to have its own version of the preferences

Use the JavaFx vocabulary :

- `setPreferenceProperty(ObjectProperty<Preference>)` to set the preference for a given object ;
- observe the corresponding `ObjectProperty`

