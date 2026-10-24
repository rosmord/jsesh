<script setup lang="ts">
// A text field for the raw MdC. The model only follows *valid* MdC: while the user types something
// which cannot be read (yet), the field is flagged and the model keeps the last good text.
import { parseMdc } from "@jsesh/core";
import { ref, watch } from "vue";

const model = defineModel<string>({ required: true });

const text = ref(model.value);
const error = ref<string>();

// The model changed from outside (typing in the editor, another document...).
watch(model, (value) => {
  if (value === text.value) return;
  text.value = value;
  error.value = undefined;
});

function onInput(event: Event): void {
  text.value = (event.target as HTMLTextAreaElement).value;
  try {
    parseMdc(text.value);
    error.value = undefined;
    model.value = text.value;
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  }
}
</script>

<template>
  <div class="mdc-field">
    <textarea
      :value="text"
      :class="{ invalid: error }"
      rows="3"
      spellcheck="false"
      aria-label="Manuel de Codage"
      placeholder="Manuel de Codage, for instance nfr-f:r"
      @input="onInput"
    />
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>
