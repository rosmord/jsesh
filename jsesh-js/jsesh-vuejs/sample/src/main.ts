import { JSeshPlugin } from "@jsesh/vue";
import { createApp } from "vue";
import App from "./App.vue";
import "./style.css";

createApp(App)
  // Sets the page-wide options of JSesh, and registers <JSeshText>, <JSeshEditor>, <JSeshEditorToolbar> and
  // <JSeshPalette> in every component. `glyphsUrl` is where the glyph data is served.
  .use(JSeshPlugin, { glyphsUrl: new URL("glyph-data/", document.baseURI).href })
  .mount("#app");
