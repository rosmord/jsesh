package jsesh.document.events;

import jsesh.model.operations.ModelOperation;

/**
 * An event which corresponds to an edit in an existing text.
 */
public record TextOperationEvent(ModelOperation operation) implements TextEvent {    
}
