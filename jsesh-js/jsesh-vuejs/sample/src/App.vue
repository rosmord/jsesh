<script setup lang="ts">
import { JSeshEditor } from "@jsesh/vue";
import { computed, ref, useTemplateRef } from "vue";
import DocumentList from "./components/DocumentList.vue";
import MdcField from "./components/MdcField.vue";
import { useDocuments, type NotebookDocument } from "./documents";

const { documents, add, remove } = useDocuments();
const current = ref<NotebookDocument | undefined>(documents[0]);

// The editor component (its exposed commands and `state`), for the toolbar, the palette and the download.
const editor = useTemplateRef<InstanceType<typeof JSeshEditor>>("editor");

// Display options of the preview.
const size = ref(40);
const direction = ref<"ltr" | "rtl">("ltr");
const orientation = ref<"horizontal" | "vertical">("horizontal");
const showPalette = ref(true);
const previewError = ref<string>();

const typing = computed<string>(() => editor.value?.state?.code ?? "");

function addDocument(): void {
  current.value = add();
}

function removeDocument(doc: NotebookDocument): void {
  if (!confirm(`Delete “${doc.title}”?`)) return;
  remove(doc);
  if (current.value?.id === doc.id) current.value = documents[0];
}

/// Offers the text as a JSesh document (.gly), which the JSesh application can open.
function download(): void {
  if (!current.value || !editor.value) return;
  const blob = new Blob([editor.value.document()], { type: "text/plain;charset=utf-8" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `${current.value.title || "text"}.gly`;
  link.click();
  URL.revokeObjectURL(link.href);
}
</script>

<template>
  <div class="app">
    <header>
      <h1>Hieroglyphic notebook</h1>
      <p>A small Vue 3 application on <code>@jsesh/vue</code>.</p>
    </header>

    <DocumentList
      :documents="documents"
      :current-id="current?.id"
      @select="current = $event"
      @add="addDocument"
      @remove="removeDocument"
    />

    <main v-if="current">
      <input v-model="current.title" class="title" aria-label="Title" />

      <!-- The `key` gives each document its own editor, with its own undo history. -->
      <section>
        <JSeshEditorToolbar :editor="editor">
          <label><input v-model="showPalette" type="checkbox" /> Palette</label>
          <button type="button" @click="download">Download .gly</button>
        </JSeshEditorToolbar>
        <JSeshEditor :key="current.id" ref="editor" v-model:mdc="current.mdc" :cadrat-height="45" />
        <p class="status">
          Typing: <code>{{ typing || "–" }}</code>
          <span v-if="editor?.state?.error" class="error">{{ editor.state.error }}</span>
        </p>
      </section>

      <section>
        <h2>Manuel de Codage</h2>
        <MdcField v-model="current.mdc" />
      </section>

      <section>
        <h2>Preview</h2>
        <div class="options">
          <label>Size <input v-model.number="size" type="range" min="20" max="90" /></label>
          <label>
            Direction
            <select v-model="direction">
              <option value="ltr">left to right</option>
              <option value="rtl">right to left</option>
            </select>
          </label>
          <label>
            Orientation
            <select v-model="orientation">
              <option value="horizontal">lines</option>
              <option value="vertical">columns</option>
            </select>
          </label>
        </div>
        <div class="preview">
          <JSeshText
            :mdc="current.mdc"
            :cadrat-height="size"
            :direction="direction"
            :orientation="orientation"
            @render="previewError = undefined"
            @error="previewError = String($event)"
          />
        </div>
        <p v-if="previewError" class="error">{{ previewError }}</p>
      </section>
    </main>
    <main v-else><p>Create a text to start.</p></main>

    <!-- A chosen sign (double click, or the Choose button) goes into the editor, at the cursor. -->
    <aside v-show="showPalette && current">
      <JSeshPalette @sign-chosen="editor?.insertSign($event); editor?.focus()" />
    </aside>
  </div>
</template>
