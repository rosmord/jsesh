package jsesh.mdc.model;

import jsesh.mdc.model.operations.ModelOperation;

public interface ElementObserver {
	void elementChanged(ModelOperation op);
}
