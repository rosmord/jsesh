/*
 * Created on 20 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.model.operations;

/**
 * A visitor for the various kinds of operations on models. 
 *   @author rosmord
 *
 */
public interface ModelOperationVisitor {

	/**
	 * @param insertion
	 */
	void visitInsertion(Insertion insertion);

	/**
	 * @param deletion
	 */
	void visitDeletion(Deletion deletion);

	/**
	 * @param replacement
	 */
	void visitReplacement(Replacement replacement);

	/**
	 * @param modification
	 */
	void visitModification(Modification modification);

	/**
	 * @param operation
	 */
	void visitChildOperation(ChildOperation operation);

	/**
	 * @param modification
	 */
	void visitZoneModification(ZoneModification modification);
	

}
