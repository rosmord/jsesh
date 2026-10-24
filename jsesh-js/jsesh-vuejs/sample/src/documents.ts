import { reactive, watch } from "vue";

export interface NotebookDocument {
  id: number;
  title: string;
  /// The text, in Manuel de Codage.
  mdc: string;
}

const STORAGE_KEY = "jsesh-vue-sample:documents";

const EXAMPLES: Omit<NotebookDocument, "id">[] = [
  { title: "Good", mdc: "nfr-f:r" },
  { title: "Offering formula (start)", mdc: "M23-X1:R4-X8_-D4:Q1-A40_-V30_:R11-D46:O49_-W18-N35:X1_-R13_-V30_:U23-D58-N26:O49_" },
  {title: "Cartouche", mdc: "<-N5-Y5:N35-L1->"},
  { title: "Osiris", mdc: "D4:Q1-A40_" },
];

function load(): NotebookDocument[] {
  try {
    const stored: unknown = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? "null");
    if (Array.isArray(stored) && stored.length > 0) return stored;
  } catch {
    // No storage, or damaged: start again.
  }
  return EXAMPLES.map((doc, i) => ({ id: i + 1, ...doc }));
}

/// The documents of the notebook, kept in the browser's `localStorage`.
export function useDocuments() {
  const documents = reactive(load());

  watch(
    documents,
    () => {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(documents));
      } catch {
        // Not kept, then.
      }
    },
    { deep: true },
  );

  function add(): NotebookDocument {
    const doc = reactive<NotebookDocument>({
      id: Math.max(0, ...documents.map((d) => d.id)) + 1,
      title: "New text",
      mdc: "",
    });
    documents.push(doc);
    return doc;
  }

  function remove(doc: NotebookDocument): void {
    const index = documents.findIndex((d) => d.id === doc.id);
    if (index >= 0) documents.splice(index, 1);
  }

  return { documents, add, remove };
}
