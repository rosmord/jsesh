package jsesh.editor.events;

import jsesh.mdc.model.operations.ModelOperation;

/**
 * An event which corresponds to an edit in an existing text.
 */
public record TextOperationEvent(ModelOperation operation) implements TextEvent {    
}
