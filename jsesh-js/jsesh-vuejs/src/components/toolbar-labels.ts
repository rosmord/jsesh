/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The texts of `JSeshEditorToolbar`'s buttons.
export interface ToolbarLabels {
  undo: string;
  redo: string;
  stack: string;
  sideBySide: string;
  ligature: string;
  cartouche: string;
  red: string;
}

export const DEFAULT_TOOLBAR_LABELS: ToolbarLabels = {
  undo: "Undo",
  redo: "Redo",
  stack: "Stack",
  sideBySide: "Side by side",
  ligature: "Ligature",
  cartouche: "Cartouche",
  red: "Red",
};
