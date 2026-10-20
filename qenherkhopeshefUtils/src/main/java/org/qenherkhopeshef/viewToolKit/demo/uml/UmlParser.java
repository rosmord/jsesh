package org.qenherkhopeshef.viewToolKit.demo.uml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qenherkhopeshef.viewToolKit.demo.uml.UmlModel.ClassKind;
import org.qenherkhopeshef.viewToolKit.demo.uml.UmlModel.UmlClass;
import org.qenherkhopeshef.viewToolKit.demo.uml.UmlModel.UmlRelation;
import org.qenherkhopeshef.viewToolKit.drawing.diagram.ConnectorEnd;

/// Reads a small subset of the PlantUML class diagram syntax.
///
/// ```
/// abstract class Shape {
///   - origin : Point
///   + draw(g : Graphics)
/// }
/// interface Drawable
/// Drawable <|.. Shape
/// Shape <|-- Circle
/// Drawing "1" *-- "*" Shape : contains
/// ```
///
/// Relations: `--` (solid) or `..` (dashed), optionally decorated on each side
/// by `<|`/`|>` (triangle), `*` (filled diamond), `o` (hollow diamond) or
/// `<`/`>` (arrow). Quoted labels may be given near each end, and a label
/// after `:`. Classes used in relations are created if needed. Lines starting
/// with `'` are comments.
final class UmlParser {

	static final class UmlParseException extends Exception {
		private static final long serialVersionUID = 1L;
		final int line;

		UmlParseException(int line, String message) {
			super("line " + line + ": " + message);
			this.line = line;
		}
	}

	private static final Pattern CLASS_DECLARATION = Pattern.compile(
			"^(abstract\\s+class|abstract|class|interface|enum)\\s+(\\w+)\\s*(\\{)?\\s*(\\})?$");

	private static final Pattern RELATION = Pattern.compile(
			"^(\\w+)\\s*(?:\"([^\"]*)\"\\s*)?(<\\||[<*o])?(--+|\\.\\.+)(\\|>|[>*o])?\\s*(?:\"([^\"]*)\"\\s*)?(\\w+)\\s*(?::\\s*(.*))?$");

	private UmlParser() {
	}

	static UmlModel parse(String source) throws UmlParseException {
		UmlModel model = new UmlModel();
		UmlClass current = null; // class whose body is being read.
		String[] lines = source.split("\r\n|\r|\n");
		for (int i = 0; i < lines.length; i++) {
			int lineNumber = i + 1;
			String line = lines[i].strip();
			if (line.isEmpty() || line.startsWith("'") || line.startsWith("@"))
				continue;
			if (current != null) {
				if (line.equals("}")) {
					current = null;
				} else if (line.contains("(")) {
					current.operations.add(line);
				} else {
					current.attributes.add(line);
				}
				continue;
			}
			Matcher m = CLASS_DECLARATION.matcher(line);
			if (m.matches()) {
				UmlClass c = model.getOrCreateClass(m.group(2));
				c.kind = switch (m.group(1).replaceAll("\\s+", " ")) {
				case "abstract class", "abstract" -> ClassKind.ABSTRACT_CLASS;
				case "interface" -> ClassKind.INTERFACE;
				case "enum" -> ClassKind.ENUM;
				default -> ClassKind.CLASS;
				};
				if (m.group(3) != null && m.group(4) == null)
					current = c;
				continue;
			}
			m = RELATION.matcher(line);
			if (m.matches()) {
				model.addRelation(new UmlRelation(m.group(1), m.group(7), end(m.group(3)), end(m.group(5)),
						m.group(4).startsWith("."), nonNull(m.group(2)), nonNull(m.group(6)),
						nonNull(m.group(8)).strip()));
				continue;
			}
			throw new UmlParseException(lineNumber, "can't understand \"" + line + "\"");
		}
		if (current != null)
			throw new UmlParseException(lines.length, "missing } for class " + current.name);
		return model;
	}

	private static ConnectorEnd end(String symbol) {
		if (symbol == null)
			return ConnectorEnd.NONE;
		return switch (symbol) {
		case "<|", "|>" -> ConnectorEnd.HOLLOW_TRIANGLE;
		case "*" -> ConnectorEnd.FILLED_DIAMOND;
		case "o" -> ConnectorEnd.HOLLOW_DIAMOND;
		case "<", ">" -> ConnectorEnd.OPEN_ARROW;
		default -> ConnectorEnd.NONE;
		};
	}

	private static String nonNull(String s) {
		return s == null ? "" : s;
	}
}
