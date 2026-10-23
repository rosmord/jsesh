import { defineConfig } from "vite";
import dts from "vite-plugin-dts";

export default defineConfig({
  build: {
    lib: {
      entry: "src/index.ts",
      formats: ["es"],
      fileName: () => "jsesh-core.js",
    },
    sourcemap: true,
  },
  plugins: [dts({ rollupTypes: true, tsconfigPath: "./tsconfig.json" })],
});
