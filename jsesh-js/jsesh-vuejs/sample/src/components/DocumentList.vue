<script setup lang="ts">
import type { NotebookDocument } from "../documents";

defineProps<{ documents: NotebookDocument[]; currentId: number | undefined }>();
defineEmits<{ select: [doc: NotebookDocument]; add: []; remove: [doc: NotebookDocument] }>();
</script>

<template>
  <nav class="document-list">
    <ul>
      <li v-for="doc in documents" :key="doc.id" :class="{ current: doc.id === currentId }">
        <button type="button" class="open" @click="$emit('select', doc)">
          <strong>{{ doc.title || "(untitled)" }}</strong>
          <!-- A small <JSeshText> as a thumbnail. -->
          <JSeshText :mdc="doc.mdc" :cadrat-height="22" />
        </button>
        <button type="button" class="remove" title="Delete this text" @click="$emit('remove', doc)">×</button>
      </li>
    </ul>
    <button type="button" @click="$emit('add')">+ New text</button>
  </nav>
</template>
