package jsesh.mdc.file;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.jseshInfo.JSeshInfoReader;
import jsesh.utils.EnumBase;

/**
 * Immutable (?) representation of document preferences.
 * 
 * Note about this: We would like to avoid code duplication for property reading
 * and writing in {@link JSeshInfoReader} and in the main application.
 * 
 * Now, we can either :
 * <ul>
 * <li>build our own property-based system... we use such a system in Ramses
 * (for graphical elements properties). It's a tempting idea.
 * <li>create i/o oriented properties as "meta" information using introspection.
 * basically : a property has a name (which might include the class ?/ have a
 * prefix ?), a type, and belongs to either a mutable or an immutable object.
 * <p>
 * They can be declared or discovered through introspection.
 * </ul>
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class DocumentPreferences {

	@SaveAbleProperty(JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED)
	private boolean smallSignCentered = false;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_PAGE_ORIENTATION)
	private TextOrientation textOrientation = TextOrientation.HORIZONTAL;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_PAGE_DIRECTION)
	private TextDirection textDirection = TextDirection.LEFT_TO_RIGHT;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH)
	private double cartoucheLineWidth = 1;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_MAX_QUADRANT_WIDTH)
	private double maxQuadrantWidth = 22;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT)
	private double maxQuadrantHeight = 18;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_LINE_SKIP)
	private double lineSkip = 6;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_COLUMN_SKIP)
	private double columnSkip = 8;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING)
	private boolean useLinesForShading = false;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_STANDARD_SIGN_HEIGHT)
	private double standardSignHeight = 18;
	@SaveAbleProperty(JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT)
	private double smallBodyScaleLimit = 12;

	public DocumentPreferences() {
	}

	/**
	 * Private copy builder
	 * 
	 * @param o
	 */
	private DocumentPreferences copy() {
		// Lazy and slow, but will survive any change... other option would be
		// to use a inner cloneable class as representation.
		return DocumentPreferences
				.fromStringMap(this.getStringRepresentation());
	}

	/**
	 * Init document preferences from a map of values. (I might be reinventing
	 * java beans here ????).
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DocumentPreferences fromStringMap(Map<String, String> map) {
		try {
			DocumentPreferences result = new DocumentPreferences();
			HashMap<String, Field> fieldMap = new HashMap<String, Field>();
			for (Field field : DocumentPreferences.class.getDeclaredFields()) {
				SaveAbleProperty prop = field
						.getAnnotation(SaveAbleProperty.class);
				if (prop != null) {
					fieldMap.put(prop.value(), field);
				}
			}
			for (String pref : map.keySet()) {
				if (fieldMap.containsKey(pref)) {
					String stringValue = map.get(pref);
					Field field = fieldMap.get(pref);
					if (field.getType().isEnum()) {
						// Problems with type erasure here...
						Class clazz = field.getType();
						field.set(result, Enum.valueOf(clazz, stringValue));
					} else if (Boolean.TYPE.equals(field.getType())) {
						// Compatibility with "old" files
						if ("1".equals(stringValue))
							field.setBoolean(result, true);
						else if ("0".equals(stringValue))
							field.setBoolean(result, false);
						else
							field.setBoolean(result,
									Boolean.parseBoolean(stringValue));
					} else if (Double.TYPE.equals(field.getType())) {
						field.setDouble(result, Double.parseDouble(stringValue));
					} else
						throw new RuntimeException("Unexpected type for "
								+ pref + " complement the interface");
				}
			}
			return result;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return a string map suitable to save the preferences map
	 * 
	 * @return
	 */
	public Map<String, String> getStringRepresentation() {
		try {
			HashMap<String, String> result = new HashMap<String, String>();
			for (Field field : this.getClass().getDeclaredFields()) {
				SaveAbleProperty prop = field
						.getAnnotation(SaveAbleProperty.class);
				if (prop != null) {
					String propertyValue;
					String propertyName = prop.value();
					if (Enum.class.isAssignableFrom(field.getType())) {
						propertyValue = ((Enum<?>) field.get(this)).name();
					} else if (Boolean.TYPE.equals(field.getType())) {
						propertyValue = Boolean
								.toString(field.getBoolean(this));
					} else if (Double.TYPE.equals(field.getType())) {
						propertyValue = Double.toString(field.getDouble(this));
					} else
						throw new RuntimeException("Unexpected type for "
								+ propertyName + " complement the interface");
					result.put(propertyName, propertyValue);
				}
			}
			return result;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Move this method contents to tests.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DocumentPreferences prefs = new DocumentPreferences();
		System.out.println(prefs.getStringRepresentation());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH, "10.4");
		map.put(JSeshInfoConstants.JSESH_PAGE_DIRECTION, "RIGHT_TO_LEFT");
		map.put(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING, "true");
		DocumentPreferences prefs1 = DocumentPreferences.fromStringMap(map);
		System.out.println(prefs1.getStringRepresentation());
		System.out
				.println(prefs1.withTextOrientation(TextOrientation.VERTICAL));

	}

	// Pseudo-setters...

	/**
	 * @param smallSignCentered
	 *            the smallSignCentered to set
	 */
	public DocumentPreferences withSmallSignCentered(
			boolean newSmallSignCentered) {
		DocumentPreferences result = this.copy();
		result.smallSignCentered = newSmallSignCentered;
		return result;
	}

	/**
	 * @param textOrientation
	 *            the textOrientation to set
	 */
	public DocumentPreferences withTextOrientation(
			TextOrientation textOrientation) {
		if (textOrientation == null)
			throw new NullPointerException("TextOrientation should not be null");
		DocumentPreferences result = this.copy();
		result.textOrientation = textOrientation;
		return result;
	}

	/**
	 * @param textDirection
	 *            the textDirection to set
	 */
	public DocumentPreferences withTextDirection(TextDirection textDirection) {
		if (textDirection == null)
			throw new NullPointerException("TextDirection should not be null");
		DocumentPreferences result = this.copy();
		result.textDirection = textDirection;
		return result;
	}

	/**
	 * @param cartoucheLineWidth
	 *            the cartoucheLineWidth to set
	 */
	public DocumentPreferences withCartoucheLineWidth(double cartoucheLineWidth) {
		DocumentPreferences result = this.copy();
		result.cartoucheLineWidth = cartoucheLineWidth;
		return result;
	}

	/**
	 * @param maxQuadrantWidth
	 *            the maxQuadrantWidth to set
	 */
	public DocumentPreferences withMaxQuadrantWidth(double maxQuadrantWidth) {
		DocumentPreferences result = this.copy();
		result.maxQuadrantWidth = maxQuadrantWidth;
		return result;
	}

	/**
	 * @param maxQuadrantHeight
	 *            the maxQuadrantHeight to set
	 */
	public DocumentPreferences withMaxQuadrantHeight(double maxQuadrantHeight) {
		DocumentPreferences result = this.copy();
		result.maxQuadrantHeight = maxQuadrantHeight;
		return result;
	}

	/**
	 * @param lineSkip
	 *            the lineSkip to set
	 */
	public DocumentPreferences withLineSkip(double lineSkip) {
		DocumentPreferences result = this.copy();
		result.lineSkip = lineSkip;
		return result;
	}

	/**
	 * @param columnSkip
	 *            the columnSkip to set
	 */
	public DocumentPreferences withColumnSkip(double columnSkip) {
		DocumentPreferences result = this.copy();
		result.columnSkip = columnSkip;
		return result;
	}

	/**
	 * @param useLinesForShading
	 *            the useLinesForShading to set
	 */
	public DocumentPreferences withUseLinesForShading(boolean useLinesForShading) {
		DocumentPreferences result = this.copy();
		result.useLinesForShading = useLinesForShading;
		return result;
	}

	/**
	 * @param standardSignHeight
	 *            the standardSignHeight to set
	 */
	public DocumentPreferences withStandardSignHeight(double standardSignHeight) {
		DocumentPreferences result = this.copy();
		result.standardSignHeight = standardSignHeight;
		return result;
	}

	/**
	 * @param smallBodyScaleLimit
	 *            the smallBodyScaleLimit to set
	 */
	public DocumentPreferences withSmallBodyScaleLimit(
			double smallBodyScaleLimit) {
		DocumentPreferences result = this.copy();
		result.smallBodyScaleLimit = smallBodyScaleLimit;
		return result;
	}

	// Generated code
	/**
	 * @return the smallSignCentered
	 */
	public boolean isSmallSignCentered() {
		return smallSignCentered;
	}

	/**
	 * @return the textOrientation
	 */
	public TextOrientation getTextOrientation() {
		return textOrientation;
	}

	/**
	 * @return the textDirection
	 */
	public TextDirection getTextDirection() {
		return textDirection;
	}

	/**
	 * @return the cartoucheLineWidth
	 */
	public double getCartoucheLineWidth() {
		return cartoucheLineWidth;
	}

	/**
	 * @return the maxQuadrantWidth
	 */
	public double getMaxQuadrantWidth() {
		return maxQuadrantWidth;
	}

	/**
	 * @return the maxQuadrantHeight
	 */
	public double getMaxQuadrantHeight() {
		return maxQuadrantHeight;
	}

	/**
	 * @return the lineSkip
	 */
	public double getLineSkip() {
		return lineSkip;
	}

	/**
	 * @return the columnSkip
	 */
	public double getColumnSkip() {
		return columnSkip;
	}

	/**
	 * @return the useLinesForShading
	 */
	public boolean isUseLinesForShading() {
		return useLinesForShading;
	}

	/**
	 * @return the standardSignHeight
	 */
	public double getStandardSignHeight() {
		return standardSignHeight;
	}

	/**
	 * @return the smallBodyScaleLimit
	 */
	public double getSmallBodyScaleLimit() {
		return smallBodyScaleLimit;
	}

	@Override
	public String toString() {
		return getStringRepresentation().toString();
	}

}

// A first try at annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface SaveAbleProperty {
	String value();
}
