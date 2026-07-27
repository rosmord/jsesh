*Released 2008/11/09*

* IMPORTANT BUG FIX: the scale used in "edited groups" was wrong since august 2008 (I typed 10000 instead of 1000 when cleaning up the drawingSpecification system). Old groups did not print well as a result of this. Of course, with this bug fix, 
you will need to re-arrange groups in text written with the faulty systems.
* Usability improvement: cut and paste of right-to-left text or text in column did give strange results with the default settings. I have fixed this, and introduced a new option in the preferences: "respect layout", which is "on" by default. When it's "off", JSesh uses left-to-right text. A new "copy" mode has been introduced: wysiwyg. Note that right-to-left cut and paste will probably be improved at some time, because the left-to-right default is not very "international" (it doesn't mix well with arabic or hebrew texts, for instance).
* And, once more, a whole new family of font have been updated. JSesh has now a complete "N" family from S. Thomas. I have also worked on the glyphs description, and the "man and its occupation" family is on its way. More to come

While I'm saying this, I will add that JSesh includes a tool to describe glyphs, which can be found in the "bin" folder. Anyone with a reasonable knowledge of hieroglyphs can help... The glyph description data can only be created "by hand", and it takes time. It's also a task that can be easily shared by many people. So, consider this as a call to volunteers :)
