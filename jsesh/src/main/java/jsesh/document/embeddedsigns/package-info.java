/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
/// Signs drawn by the user and stored in a document ("embedded signs",
/// `SIGN1`, `SIGN2`...).
///
/// - [jsesh.document.embeddedsigns.EmbeddedSignList] holds the signs of one
///   document ([jsesh.document.MDCDocument#getEmbeddedSigns()]);
/// - [jsesh.document.embeddedsigns.EmbeddedSignShapeRepository] and
///   [jsesh.document.embeddedsigns.EmbeddedSignPossibilityRepository] make them
///   drawable and typable, layered over the global resources;
/// - the header format is read and written by `jsesh.io.document.EmbeddedSignsHeaderCodec`.
package jsesh.document.embeddedsigns;
