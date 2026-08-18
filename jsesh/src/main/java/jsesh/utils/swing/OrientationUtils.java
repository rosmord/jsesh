/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.swing;

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
