package jsesh.swing.utils;

public class OrientationUtils {
 private OrientationUtils() {    
 }

 /**
  * Apply the locale orientation (left-to-right or right-to-left) to a component and its children.
  * @param component
  */
 public static void fixComponentOrientation(java.awt.Component component) {
     component.applyComponentOrientation(
             java.awt.ComponentOrientation.getOrientation(java.util.Locale.getDefault()));
 }
}
