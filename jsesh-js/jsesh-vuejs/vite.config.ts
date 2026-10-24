import vue from "@vitejs/plugin-vue";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [
    vue({
      // <jsesh-text>, <jsesh-editor> and <jsesh-palette> are Web Components, not Vue components.
      template: { compilerOptions: { isCustomElement: (tag) => tag.startsWith("jsesh-") } },
    }),
  ],
  build: {
    lib: {
      entry: "src/index.ts",
      formats: ["es"],
      fileName: () => "jsesh-vue.js",
    },
    rollupOptions: { external: ["vue", /^@jsesh\//] },
    sourcemap: true,
  },
});
