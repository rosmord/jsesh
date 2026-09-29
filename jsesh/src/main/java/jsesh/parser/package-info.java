/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
/// Code for reading texts written in the Manuel de Codage format.
///
/// This package builds a literal, uninterpreted record of what was parsed:
/// [jsesh.parser.MDCParser], a hand-written recursive descent parser for the
/// Manuel de Codage grammar, returns an [jsesh.parser.ast.AstDocument],
/// walkable with [jsesh.parser.ast.AstVisitor]. See the package
/// [jsesh.parser.ast] for details.
///
/// It does not depend on [jsesh.model]. To get an in-memory document
/// model instead, which is what you will want most of the time, use
/// [jsesh.mdcreader.MDCParserModelGenerator], which interprets this AST.
package jsesh.parser;
