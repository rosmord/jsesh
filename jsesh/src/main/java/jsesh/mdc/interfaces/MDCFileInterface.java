package jsesh.mdc.interfaces;


/**
 * Marker interface used by the parser/builder pattern.
 * <p>Originally, the idea was to avoid having a stack for building object, and still keep
 * some type safety. But it polutes the model with I/O related code.
 * 
 * <p>It might not be such a great idea in the long run. 
 */
public interface MDCFileInterface {};
