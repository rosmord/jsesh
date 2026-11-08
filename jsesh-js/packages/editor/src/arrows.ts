/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// How the arrow keys move the cursor (like the Java `jsesh.ui.editor.arrows.ArrowKeyPolicy`).
///
/// All the logic of a way to handle the arrows lives in its policy, so that `EditorSession` doesn't change when the
/// choice changes. The port only moves along the top items of the text: changing line or column with the arrows is
/// not supported yet.

/// What a policy needs to know about the text being edited.
export interface ArrowContext {
  readonly isRightToLeft: boolean;
  readonly isVertical: boolean;
}

export interface ArrowKeyPolicy {
  /// The move for an arrow key (`"ArrowRight"`...): +1 forward, -1 backward, or `undefined` if the key does nothing.
  step(key: string, context: ArrowContext): number | undefined;
}

/// Arrows follow the screen: the right arrow moves to the right, that is backward in right-to-left text (the
/// behaviour of JSesh up to 9.0). In vertical text, Down and Up also move forward and backward.
export class VisualArrowKeyPolicy implements ArrowKeyPolicy {
  step(key: string, context: ArrowContext): number | undefined {
    const rtl = context.isRightToLeft;
    const steps: Record<string, number> = { ArrowRight: rtl ? -1 : 1, ArrowLeft: rtl ? 1 : -1 };
    if (context.isVertical) Object.assign(steps, { ArrowDown: 1, ArrowUp: -1 });
    return steps[key];
  }
}

/// Logical arrows (like the Java `LogicalArrowKeyPolicy`): they never depend on the text. The direction ("ltr", the
/// default, or "rtl") tells which horizontal arrow goes forward; the orientation ("horizontal", the default, for lines,
/// or "vertical" for columns) tells whether Left/Right (lines) or Up/Down (columns) move in the text.
export class LogicalArrowKeyPolicy implements ArrowKeyPolicy {
  constructor(
    readonly direction: "ltr" | "rtl" = "ltr",
    readonly orientation: "horizontal" | "vertical" = "horizontal",
  ) {}

  step(key: string): number | undefined {
    const forward = this.direction === "ltr" ? 1 : -1;
    const steps: Record<string, number> =
      this.orientation === "horizontal" ? { ArrowRight: forward, ArrowLeft: -forward } : { ArrowDown: 1, ArrowUp: -1 };
    return steps[key];
  }
}
