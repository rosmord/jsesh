package jsesh.model;

import jsesh.model.operations.ModelOperation;

public interface ElementObserver {
	void elementChanged(ModelOperation op);
}
