*Released 2009/11/12*

Version 2.13.3 of JSesh is out! 

(bug fix for 2.13.2)

New features:

* S. J. P. Thomas have sent me a nice complement for the "K" family, which is now complete.

* Ff4 sign : the "hear with hair" variant of F21 is available.

* huge improvement in the rendering of editorial parenthesis ([..], {..}, etc...) The size of the parenthesis is now computed from their environment. That is, [[\*p\*]]*t:pt should display reasonably well. There might be a number of small problems with old Tksesh files. I'll fix this later.

* features to support the MacScribe-to-JSesh converter: short bits of texts can be manipulated as glyphs. They are encoded between ".." in the Manuel de Codage encoding (there is no user interface for this feature yet).

    Individual signs can be shaded with the \shading modifier. Example : t:A\shading12 will have the top of the aleph sign shaded, but not the "t". There is no user interface for this feature yet.

* a few bug fixes (quarter-shading symbols where incorrectly saved, for instance).

* PDF cut and paste on mac is now much faster (I have removed  a large bunch of useless slow code there)
