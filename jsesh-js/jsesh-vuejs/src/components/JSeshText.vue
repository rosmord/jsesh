<!--
  Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
  SPDX-License-Identifier: CECILL-C
-->
<script setup lang="ts">
// Displays a hieroglyphic text. It is `<jsesh-text>` (`@jsesh/elements`), with Vue props:
//
//   <JSeshText mdc="i-w-r:a-C1-m-p*t:pt" :cadrat-height="40" direction="rtl" />
//
// An invalid text does not throw: the element shows it as plain text and `error` is emitted,
// so a live preview can follow a text field at each keystroke.
import type { StyleChanges } from "@jsesh/core";
import "@jsesh/elements";
import { useTemplateRef } from "vue";

defineProps<{
  /// The text, in Manuel de Codage (or a whole JSesh document, header included).
  mdc: string;
  /// Height of a quadrat, in pixels (by default, 1.5 times the font size of the surroundings).
  cadratHeight?: number;
  direction?: "ltr" | "rtl";
  orientation?: "horizontal" | "vertical";
  /// Center small signs in their quadrat.
  centered?: boolean;
  /// Justify the lines.
  justified?: boolean;
  /// Glyph data for this text only (see `configureJSesh` for the whole page).
  glyphsUrl?: string;
  /// Any change to the style: colours, line widths, fonts... (see `JSeshStyle` in `@jsesh/core`).
  jseshStyle?: StyleChanges;
}>();

const emit = defineEmits<{
  /// The text has been drawn.
  render: [];
  /// The text could not be drawn (invalid MdC, glyph data not found...).
  error: [error: unknown];
}>();

const element = useTemplateRef("element");
defineExpose({ element });
</script>

<template>
  <!-- Boolean attributes: present (with any value) means true, so `false` must remove them. -->
  <jsesh-text
    ref="element"
    :mdc.prop="mdc"
    :cadrat-height="cadratHeight"
    :direction="direction"
    :orientation="orientation"
    :centered="centered ? '' : undefined"
    :justified="justified ? '' : undefined"
    :glyphs-url="glyphsUrl"
    :jseshStyle.prop="jseshStyle"
    @jsesh-render="emit('render')"
    @jsesh-error="emit('error', ($event as CustomEvent).detail)"
  />
</template>
