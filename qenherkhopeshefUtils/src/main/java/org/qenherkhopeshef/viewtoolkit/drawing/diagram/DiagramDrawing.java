package org.qenherkhopeshef.viewtoolkit.drawing.diagram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qenherkhopeshef.viewtoolkit.drawing.GridDrawing;
import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit.drawing.event.DrawingEvent;

/// A drawing made of nodes (any graphical element) and of connectors between
/// them.
///
/// Connectors are kept up to date: when a node is moved or resized, the
/// connectors attached to it are re-routed.
///
/// Connectors are put on layer -1 by [#addConnector(ConnectorElement)], so
/// that they are drawn under the nodes.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class DiagramDrawing extends GridDrawing {

	/// Distance between parallel connectors.
	private static final double PARALLEL_SPACING = 14;

	private final Map<GraphicalElement, Set<ConnectorElement>> connectorsByNode = new HashMap<>();

	public DiagramDrawing() {
		super(150, 150);
	}

	/// Adds a connector. Its ends should already be in the drawing (or be
	/// added later).
	public void addConnector(ConnectorElement connector) {
		connector.setLayer(-1);
		connectorsByNode.computeIfAbsent(connector.getSource(), k -> new LinkedHashSet<>()).add(connector);
		connectorsByNode.computeIfAbsent(connector.getTarget(), k -> new LinkedHashSet<>()).add(connector);
		connector.updateGeometry();
		addElement(connector);
		spreadParallelConnectors(connector.getSource(), connector.getTarget());
	}

	/// Gives different offsets to the connectors linking the same two nodes,
	/// so that they are drawn side by side.
	private void spreadParallelConnectors(GraphicalElement a, GraphicalElement b) {
		if (a == b)
			return;
		List<ConnectorElement> parallel = new ArrayList<>();
		for (ConnectorElement c : getConnectors(a)) {
			if (c.getSource() == b || c.getTarget() == b)
				parallel.add(c);
		}
		for (int i = 0; i < parallel.size(); i++) {
			double offset = (i - (parallel.size() - 1) / 2.0) * PARALLEL_SPACING;
			ConnectorElement c = parallel.get(i);
			// Offsets are relative to the direction of the line: use the same
			// reference direction (from a to b) for all.
			c.setOffset(c.getSource() == a ? offset : -offset);
		}
	}

	/// Returns the connectors attached to a node.
	public Set<ConnectorElement> getConnectors(GraphicalElement node) {
		return Collections.unmodifiableSet(connectorsByNode.getOrDefault(node, Collections.emptySet()));
	}

	@Override
	public void putAt(GraphicalElement elt, double x, double y) {
		super.putAt(elt, x, y);
		updateConnectors(elt);
	}

	/// Removes an element. Removing a node also removes its connectors.
	@Override
	public void remove(GraphicalElement elt) {
		if (elt instanceof ConnectorElement connector) {
			unregister(connector.getSource(), connector);
			unregister(connector.getTarget(), connector);
			super.remove(elt);
			spreadParallelConnectors(connector.getSource(), connector.getTarget());
		} else {
			for (ConnectorElement connector : new ArrayList<>(getConnectors(elt)))
				remove(connector);
			super.remove(elt);
		}
	}

	private void unregister(GraphicalElement node, ConnectorElement connector) {
		Set<ConnectorElement> set = connectorsByNode.get(node);
		if (set != null) {
			set.remove(connector);
			if (set.isEmpty())
				connectorsByNode.remove(node);
		}
	}

	@Override
	public void clear() {
		connectorsByNode.clear();
		super.clear();
	}

	@Override
	protected void eventOccurredInDrawing(DrawingEvent ev) {
		super.eventOccurredInDrawing(ev);
		GraphicalElement source = ev.getSource();
		if (source != null && !(source instanceof ConnectorElement))
			updateConnectors(source);
	}

	private void updateConnectors(GraphicalElement node) {
		List<ConnectorElement> connectors = new ArrayList<>(getConnectors(node));
		for (ConnectorElement connector : connectors)
			connector.updateGeometry();
	}
}
