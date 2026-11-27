# The jsesh-templates sub module

This module contains the document templates proposed by *File > New from Template…*: ready-made layouts which are frequently needed. It is organised like the `jsesh-texts` module.

For now, the templates are those bundled with the software: users can't add their own.

## Writing a template

A template is an ordinary `.gly` file, in UTF-8, which starts with a *template header*, ended by a line containing only the text `END`:

```
++JSesh_Info 2.0 +s
++JSesh_unit 18 +s
++JSesh_layout columns +s
+bText in columns+s-!
+lFirst line of the description.+s-!
+lSecond line of the description.+s-!
+bEND+s-!
A1-B1-!
```

- The `++JSesh_...` lines are the usual document preferences. They are kept in the new document.
- The first line of text is the **name** of the template.
- The following lines, up to `END`, are its **description**.
- Everything after the `END` line is the content of the new document.

Only the text of the header lines is used (hieroglyphs in the header are ignored). The script (`+b`, `+l`, `+i`...) is free. A "line" is a line of the MdC text, ended by `!`.

## Layout

The templates live in `src/main/assets/jsesh-templates/`. The Gradle build zips `src/main/assets` into `jsesh-templates.zip` and adds it as a resource at `jsesh/jseshtemplates/jsesh-templates.zip`. No manual step is needed.

`JSeshTemplateLibrary` reads this zip. A test checks that every bundled template has a correct header and parses without error.
