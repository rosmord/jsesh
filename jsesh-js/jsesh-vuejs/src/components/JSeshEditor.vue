<!--
  Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
  SPDX-License-Identifier: CECILL-C
-->
<script setup lang="ts">
// A hieroglyphic editor: `<jsesh-editor>` (`@jsesh/editor`) with a `v-model`.
//
//   <JSeshEditor v-model:mdc="text" :cadrat-height="40" ref="editor" />
//   <button @click="editor.insertSign('A1')">A1</button>
//
// The model is the text as MdC. Typing in the editor updates it; changing it from outside replaces the editor's
// text (and the editor's undo history keeps the old one: give the component a `:key` to start afresh on a
// different document).
//
// The commands of the editor (`insertSign`, `undo`...) and its `state` are exposed, for toolbars of your own
// (or see `JSeshEditorToolbar`).
import type { StyleChanges } from "@jsesh/core";
import type { EditorSession, EditorState, JSeshEditorElement } from "@jsesh/editor";
import "@jsesh/editor";
import { onMounted, shallowRef, useTemplateRef } from "vue";

const mdc = defineModel<string>("mdc", { default: "" });

defineProps<{
  /// Height of a quadrat, in pixels (45 by default).
  cadratHeight?: number;
  direction?: "ltr" | "rtl";
  orientation?: "horizontal" | "vertical";
  glyphsUrl?: string;
  /// Any change to the style (see `JSeshStyle` in `@jsesh/core`).
  jseshStyle?: StyleChanges;
}>();

const emit = defineEmits<{
  /// After each change of the text, the cursor or the code being typed (`state` is a snapshot).
  change: [state: EditorState];
  /// The text could not be drawn (glyph data not found...).
  error: [error: unknown];
}>();

const element = useTemplateRef<JSeshEditorElement>("element");

/// What is shown around the editor: cursor, selection, code being typed, `canUndo`... `undefined` until mounted.
const state = shallowRef<EditorState>();

onMounted(() => (state.value = element.value?.state));

function onChange(event: Event): void {
  const newState = (event as CustomEvent<EditorState>).detail;
  state.value = newState;
  mdc.value = newState.mdc;
  emit("change", newState);
}

defineExpose({
  /// The `<jsesh-editor>` element.
  element,
  state,
  /// The `EditorSession` behind the element, for what is not forwarded below.
  get session(): EditorSession | undefined {
    return element.value?.session;
  },
  /// Insert a sign at the cursor, as if typed.
  insertSign: (code: string) => element.value?.insertSign(code),
  /// Insert MdC text at the cursor (replacing the selection).
  insertMdc: (text: string) => element.value?.insertMdc(text),
  /// Group the selection (or the two quadrats before the cursor) with `:`, `*` or `&`.
  group: (separator: ":" | "*" | "&") => element.value?.group(separator),
  cartouche: () => element.value?.cartouche(),
  toggleRed: () => element.value?.toggleRed(),
  undo: () => element.value?.undo(),
  redo: () => element.value?.redo(),
  /// The text as a JSesh document (the content of a `.gly` file).
  document: () => element.value?.document() ?? "",
  focus: () => element.value?.focus(),
});
</script>

<template>
  <jsesh-editor
    ref="element"
    :mdc.prop="mdc"
    :cadrat-height="cadratHeight"
    :direction="direction"
    :orientation="orientation"
    :glyphs-url="glyphsUrl"
    :jseshStyle.prop="jseshStyle"
    @jsesh-change="onChange"
    @jsesh-error="emit('error', ($event as CustomEvent).detail)"
  />
</template>
