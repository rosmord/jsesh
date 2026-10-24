<!--
  Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
  SPDX-License-Identifier: CECILL-C
-->
<script setup lang="ts">
// The usual commands of a `JSeshEditor`: undo, redo, grouping, cartouche, red.
//
//   <JSeshEditorToolbar :editor="editor" />
//   <JSeshEditor ref="editor" v-model:mdc="text" />
//
// `editor` is the template ref of the `JSeshEditor`. The buttons are plain `<button>`s in a
// `div.jsesh-toolbar`: style them from your CSS. Put your own buttons in the default slot.
import { computed } from "vue";
import type JSeshEditor from "./JSeshEditor.vue";
import { DEFAULT_TOOLBAR_LABELS, type ToolbarLabels } from "./toolbar-labels.js";

const props = defineProps<{
  /// The editor to command (a template ref to a `JSeshEditor`).
  editor: InstanceType<typeof JSeshEditor> | null | undefined;
  /// Translations of the buttons; the missing ones stay in English.
  labels?: Partial<ToolbarLabels>;
}>();

const text = computed(() => ({ ...DEFAULT_TOOLBAR_LABELS, ...props.labels }));
const canUndo = computed(() => props.editor?.state?.canUndo === true);
const canRedo = computed(() => props.editor?.state?.canRedo === true);

/// Runs a command, then gives the keyboard back to the editor.
function run(command: (editor: NonNullable<typeof props.editor>) => void): void {
  const editor = props.editor;
  if (editor == null) return;
  command(editor);
  editor.focus();
}
</script>

<template>
  <div class="jsesh-toolbar" role="toolbar" style="display: flex; flex-wrap: wrap; gap: 0.4rem; align-items: center">
    <button type="button" :disabled="!canUndo" @click="run((e) => e.undo())">{{ text.undo }}</button>
    <button type="button" :disabled="!canRedo" @click="run((e) => e.redo())">{{ text.redo }}</button>
    <button type="button" title=":" @click="run((e) => e.group(':'))">{{ text.stack }}</button>
    <button type="button" title="*" @click="run((e) => e.group('*'))">{{ text.sideBySide }}</button>
    <button type="button" title="&amp;" @click="run((e) => e.group('&'))">{{ text.ligature }}</button>
    <button type="button" @click="run((e) => e.cartouche())">{{ text.cartouche }}</button>
    <button type="button" @click="run((e) => e.toggleRed())">{{ text.red }}</button>
    <slot />
  </div>
</template>
