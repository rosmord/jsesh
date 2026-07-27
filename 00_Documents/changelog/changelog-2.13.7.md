*Released 2009/12/04*

Version 2.13.7 replaces version 2.13.5. It fixes a small (but annoying, especially for people involved in the late period :)) bug in the rendering of the W1 sign.

### About 2.13.5:

It fixes a problem in 2.13 for "old" Mac OS X machines (in this particular instance of reality, "old" means something like two years old). 

I had accidentally used features of Java 1.6, which is only supported by Mac OS X Leopard, and JSesh would not start on the older systems. I have corrected the problem - and JSesh should run on Mac from Panther to Leopard again.

I have taken advantage of this release to include new signs:

* S. J. P. Thomas has sent me the complete Aa family. So the only incomplete families now are X, Y and Z.

* I had made a poor choice for sign D45 (ḏsr). The new sign is closer to the usual shape of the hieroglyph.

### General 2.13 version features (already in 2.13.3 and 2.13.0)

* S. J. P. Thomas have sent me a nice complement for the "K" family, which is now complete.

* Ff4 sign : the "hear with hair" variant of F21 is available.

* huge improvement in the rendering of editorial parenthesis ([..], {..}, etc...) The size of the parenthesis is now computed from their environment. That is, [[\*p\*]]*t:pt should display reasonably well. There might be a number of small problems with old Tksesh files. I'll fix this later.

* features to support the MacScribe-to-JSesh converter: short bits of texts can be manipulated as glyphs. They are encoded between ".." in the Manuel de Codage encoding (there is no user interface for this feature yet).

    Individual signs can be shaded with the \shading modifier. Example : t:A\shading12 will have the top of the aleph sign shaded, but not the "t". There is no user interface for this feature yet.

* a few bug fixes (quarter-shading symbols where incorrectly saved, for instance).

* PDF cut and paste on mac is now much faster (I have removed  a large bunch of useless slow code there)
