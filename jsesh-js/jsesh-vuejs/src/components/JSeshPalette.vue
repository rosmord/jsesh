<!--
  Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
  SPDX-License-Identifier: CECILL-C
-->
<script setup lang="ts">
// JSesh's sign palette: `<jsesh-palette>` (`@jsesh/palette`) as a Vue component.
//
//   <JSeshPalette @sign-chosen="editor.insertSign($event)" />
//
// The user picks a sign by family, value, tag or shape; a double click (or the *Choose* button) emits
// `sign-chosen` with the sign's code. The sign database is loaded from `glyphsUrl`, or from the URL given
// to `configureJSesh`.
import type { SignDatabase } from "@jsesh/glyphs";
import type { JSeshPaletteElement, PaletteLabels, PaletteStorage } from "@jsesh/palette";
import "@jsesh/palette";
import { useTemplateRef } from "vue";

// The element builds the palette again each time one of `labels`, `storage` or `database` is set: change them
// rarely.
defineProps<{
  glyphsUrl?: string;
  /// Translations of the palette's texts (the missing ones stay in English).
  labels?: Partial<PaletteLabels>;
  /// Where the latest signs and the user palette are kept (`localStorage` by default).
  storage?: PaletteStorage;
  /// A sign database you have already loaded (`loadSignDatabase` in `@jsesh/glyphs`).
  database?: SignDatabase;
}>();

const emit = defineEmits<{
  /// A sign was chosen; `code` is its Gardiner code.
  "sign-chosen": [code: string];
  /// The sign database could not be loaded.
  error: [error: unknown];
}>();

const element = useTemplateRef<JSeshPaletteElement>("element");
defineExpose({
  element,
  /// Chooses the selected sign (or `code`), as the *Choose* button does.
  choose: (code?: string) => element.value?.choose(code),
});
</script>

<template>
  <jsesh-palette
    ref="element"
    :glyphs-url="glyphsUrl"
    :labels.prop="labels"
    :storage.prop="storage"
    :database.prop="database"
    @sign-chosen="emit('sign-chosen', ($event as CustomEvent<string>).detail)"
    @jsesh-error="emit('error', ($event as CustomEvent).detail)"
  />
</template>
