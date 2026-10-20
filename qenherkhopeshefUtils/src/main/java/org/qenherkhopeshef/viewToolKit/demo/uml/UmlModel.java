package org.qenherkhopeshef.viewToolKit.demo.uml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.qenherkhopeshef.viewToolKit.drawing.diagram.ConnectorEnd;

/// A (very) small UML class diagram model.
final class UmlModel {

	enum ClassKind {
		CLASS, ABSTRACT_CLASS, INTERFACE, ENUM
	}

	static final class UmlClass {
		final String name;
		ClassKind kind;
		final List<String> attributes = new ArrayList<>();
		final List<String> operations = new ArrayList<>();

		UmlClass(String name, ClassKind kind) {
			this.name = name;
			this.kind = kind;
		}
	}

	/// A relation between two classes.
	///
	/// @param from the left class in the source text.
	/// @param to the right class.
	/// @param fromEnd the decoration on the "from" side.
	/// @param toEnd the decoration on the "to" side.
	/// @param dashed dashed line (realization, dependency).
	/// @param fromLabel label near "from" (e.g. a multiplicity).
	/// @param toLabel label near "to".
	/// @param label label in the middle.
	record UmlRelation(String from, String to, ConnectorEnd fromEnd, ConnectorEnd toEnd, boolean dashed,
			String fromLabel, String toLabel, String label) {

		/// The class which should be drawn above the other one: the general
		/// class for a generalization, the whole for a composition; otherwise
		/// the class which "uses" the other one.
		String upper() {
			if (isStructuring(toEnd) && !isStructuring(fromEnd))
				return to;
			if (isStructuring(fromEnd))
				return from;
			if (fromEnd == ConnectorEnd.OPEN_ARROW && toEnd != ConnectorEnd.OPEN_ARROW)
				return to;
			return from;
		}

		String lower() {
			return upper().equals(from) ? to : from;
		}

		private static boolean isStructuring(ConnectorEnd end) {
			return end == ConnectorEnd.HOLLOW_TRIANGLE || end == ConnectorEnd.HOLLOW_DIAMOND
					|| end == ConnectorEnd.FILLED_DIAMOND;
		}
	}

	private final Map<String, UmlClass> classes = new LinkedHashMap<>();

	private final List<UmlRelation> relations = new ArrayList<>();

	/// Returns the class with this name, creating it if needed.
	UmlClass getOrCreateClass(String name) {
		return classes.computeIfAbsent(name, n -> new UmlClass(n, ClassKind.CLASS));
	}

	Collection<UmlClass> getClasses() {
		return classes.values();
	}

	void addRelation(UmlRelation relation) {
		getOrCreateClass(relation.from());
		getOrCreateClass(relation.to());
		relations.add(relation);
	}

	List<UmlRelation> getRelations() {
		return relations;
	}
}
