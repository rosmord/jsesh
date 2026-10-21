package org.qenherkhopeshef.viewtoolkit.demo.uml;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.qenherkhopeshef.viewtoolkit.demo.uml.UmlModel.UmlClass;
import org.qenherkhopeshef.viewtoolkit.demo.uml.UmlModel.UmlRelation;
import org.qenherkhopeshef.viewtoolkit.drawing.diagram.ConnectorElement;
import org.qenherkhopeshef.viewtoolkit.drawing.diagram.DiagramDrawing;

/// Builds the drawing for a [UmlModel], and places the classes.
///
/// The automatic layout is a simple layered one: general classes (and
/// "wholes" of compositions) are placed above their specializations (and
/// parts); the order of the classes in each layer is then improved with a few
/// barycenter sweeps, to reduce crossings.
final class UmlDiagramPresenter {

	private static final double MARGIN = 40;
	private static final double HORIZONTAL_GAP = 50;
	private static final double VERTICAL_GAP = 90;

	private final DiagramDrawing drawing;

	private final Map<String, ClassBoxElement> boxes = new LinkedHashMap<>();

	private UmlModel model = new UmlModel();

	UmlDiagramPresenter(DiagramDrawing drawing) {
		this.drawing = drawing;
	}

	/// Replaces the diagram.
	void show(UmlModel newModel) {
		this.model = newModel;
		drawing.clear();
		boxes.clear();
		for (UmlClass c : model.getClasses())
			boxes.put(c.name, new ClassBoxElement(c));
		layout();
		for (UmlRelation r : model.getRelations()) {
			ConnectorElement connector = new ConnectorElement(boxes.get(r.from()), boxes.get(r.to()));
			connector.setSourceEnd(r.fromEnd());
			connector.setTargetEnd(r.toEnd());
			connector.setDashed(r.dashed());
			connector.setSourceLabel(r.fromLabel());
			connector.setTargetLabel(r.toLabel());
			connector.setLabel(r.label());
			drawing.addConnector(connector);
		}
	}

	Collection<ClassBoxElement> getBoxes() {
		return boxes.values();
	}

	/// Places the class boxes (the connectors follow).
	void layout() {
		List<String> names = new ArrayList<>(boxes.keySet());
		Map<String, Integer> rank = computeRanks(names);
		int maxRank = rank.values().stream().mapToInt(Integer::intValue).max().orElse(0);
		List<List<String>> layers = new ArrayList<>();
		for (int r = 0; r <= maxRank; r++)
			layers.add(new ArrayList<>());
		for (String n : names)
			layers.get(rank.get(n)).add(n);
		reduceCrossings(layers);

		// Coordinates.
		double maxWidth = 0;
		for (List<String> layer : layers)
			maxWidth = Math.max(maxWidth, layerWidth(layer));
		double y = MARGIN;
		for (List<String> layer : layers) {
			double x = MARGIN + (maxWidth - layerWidth(layer)) / 2;
			double height = 0;
			for (String n : layer) {
				ClassBoxElement box = boxes.get(n);
				drawing.putAt(box, x, y);
				Rectangle2D b = box.getBounds();
				x += b.getWidth() + HORIZONTAL_GAP;
				height = Math.max(height, b.getHeight());
			}
			y += height + VERTICAL_GAP;
		}
	}

	private double layerWidth(List<String> layer) {
		double w = 0;
		for (String n : layer)
			w += boxes.get(n).getBounds().getWidth();
		return w + HORIZONTAL_GAP * Math.max(0, layer.size() - 1);
	}

	/// Longest-path layering, after removing the edges which would create
	/// cycles.
	private Map<String, Integer> computeRanks(List<String> names) {
		Map<String, List<String>> below = new HashMap<>();
		for (String n : names)
			below.put(n, new ArrayList<>());
		for (UmlRelation r : model.getRelations()) {
			if (!r.from().equals(r.to()))
				below.get(r.upper()).add(r.lower());
		}
		// Depth-first search: keeps acyclic edges, and builds a reverse
		// topological order.
		Map<String, Integer> state = new HashMap<>(); // 1: in progress, 2: done
		Map<String, List<String>> dag = new HashMap<>();
		List<String> postOrder = new ArrayList<>();
		for (String n : names)
			dag.put(n, new ArrayList<>());
		for (String n : names)
			if (!state.containsKey(n))
				visit(n, below, state, dag, postOrder);
		Map<String, Integer> rank = new HashMap<>();
		for (String n : names)
			rank.put(n, 0);
		for (int i = postOrder.size() - 1; i >= 0; i--) {
			String u = postOrder.get(i);
			for (String v : dag.get(u))
				rank.put(v, Math.max(rank.get(v), rank.get(u) + 1));
		}
		return rank;
	}

	private void visit(String u, Map<String, List<String>> below, Map<String, Integer> state,
			Map<String, List<String>> dag, List<String> postOrder) {
		// Iterative, to cope with long chains.
		ArrayList<String> stack = new ArrayList<>();
		ArrayList<Integer> next = new ArrayList<>();
		stack.add(u);
		next.add(0);
		state.put(u, 1);
		while (!stack.isEmpty()) {
			int top = stack.size() - 1;
			String node = stack.get(top);
			List<String> children = below.get(node);
			int k = next.get(top);
			if (k < children.size()) {
				next.set(top, k + 1);
				String child = children.get(k);
				Integer s = state.get(child);
				if (s == null) {
					dag.get(node).add(child);
					state.put(child, 1);
					stack.add(child);
					next.add(0);
				} else if (s == 2) {
					dag.get(node).add(child);
				} // s == 1: back edge, ignored.
			} else {
				state.put(node, 2);
				postOrder.add(node);
				stack.remove(top);
				next.remove(top);
			}
		}
	}

	/// Barycenter heuristic: each node moves towards the mean position of its
	/// neighbours in the adjacent layer.
	private void reduceCrossings(List<List<String>> layers) {
		Map<String, List<String>> neighbours = new HashMap<>();
		for (String n : boxes.keySet())
			neighbours.put(n, new ArrayList<>());
		for (UmlRelation r : model.getRelations()) {
			if (!r.from().equals(r.to())) {
				neighbours.get(r.from()).add(r.to());
				neighbours.get(r.to()).add(r.from());
			}
		}
		for (int sweep = 0; sweep < 4; sweep++) {
			boolean down = sweep % 2 == 0;
			for (int k = 1; k < layers.size(); k++) {
				int r = down ? k : layers.size() - 1 - k;
				List<String> reference = layers.get(down ? r - 1 : r + 1);
				List<String> layer = layers.get(r);
				Map<String, Double> barycenter = new HashMap<>();
				for (int i = 0; i < layer.size(); i++) {
					String n = layer.get(i);
					double sum = 0;
					int count = 0;
					for (String m : neighbours.get(n)) {
						int pos = reference.indexOf(m);
						if (pos >= 0) {
							sum += pos;
							count++;
						}
					}
					// Nodes without neighbours there keep (roughly) their place.
					barycenter.put(n, count == 0 ? i * (reference.size() / (double) Math.max(1, layer.size()))
							: sum / count);
				}
				layer.sort((a, b) -> Double.compare(barycenter.get(a), barycenter.get(b)));
			}
		}
	}
}
