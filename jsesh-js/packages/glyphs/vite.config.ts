import { defineConfig } from "vite";
import dts from "vite-plugin-dts";

export default defineConfig({
  build: {
    lib: {
      entry: "src/index.ts",
      formats: ["es"],
      fileName: () => "jsesh-glyphs.js",
    },
    rollupOptions: { external: [/^@jsesh\//] },
    sourcemap: true,
  },
  plugins: [dts({ rollupTypes: true, tsconfigPath: "./tsconfig.json" })],
});
