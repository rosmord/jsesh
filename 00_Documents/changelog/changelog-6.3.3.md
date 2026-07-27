*Released 2013/09/09*

This release is mainly a bug fix.

* Most important feature : fixed a bug probably introduced with JSesh 6.0 :
When a sign was selected in the Palette using a phonetic code, the software would freeze. That's fixed now.

* A number of missing signs have been added : some complete the MdC coverage (regarding the hieroglyphic), plus a few others for Unicode coverage, and finally one or two signs I have recently met.

The signs which are added because they are in the Unicode system, and not in the Hieroglyphica have a code ending in EXTU : US22F31AEXTU (sign by S. Thomas, (US22) for F31A in Unicode).

* Removed a feature originally introduced to speed up rendering, but which only took lots of memory on modern machines. Now JSesh will be less memory-hungry.
