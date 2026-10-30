/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
/// Constants of the Manuel de Codage shared by the parser's literal AST
/// (`jsesh.parser.ast`) and the document model (`jsesh.model`).
///
/// These are pure, immutable values (directions, axes, cartouche kinds...) which mean
/// exactly the same thing on both sides. Putting them here avoids duplicating them in
/// each package and converting between the copies.
///
/// This is a leaf package: it must not import any other `jsesh` package.
package jsesh.sharedvocabulary;
