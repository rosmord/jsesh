/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.glyphs.signpicture;

/// Geometry of the picture of a single sign, computed by [HieroglyphIconBuilder].
///
/// Shared by the bitmaps and the vector icons, so that both place the sign in
/// the same way.
///
/// @param width   width of the picture, in pixels.
/// @param height  height of the picture, in pixels.
/// @param scale   scale applied to the sign's shape.
/// @param xOrigin x position of the sign's top-left corner in the picture.
/// @param yOrigin y position of the sign's top-left corner in the picture.
record SignPictureLayout(int width, int height, double scale, double xOrigin, double yOrigin) {
}
