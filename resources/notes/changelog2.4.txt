While version 3 is being worked on, a preliminary stage:
* LOTS of new signs from those drawn by M. Serge Thomas. Eventually, we will cover the whole Manuel. For now, we have lots of gods.
* A NEW SIGN PALETTE. All functionnalities are not available yet, but it's a start. 
  The menus list only the basic signs. The palette will allow access to the complete set of hieroglyphs.
* for MAC users, a usable RTF output which creates MACPICT data.
* scaled inter-cadrat spaces in PDF output.

For programmers:
* Added a new Widget, the JMDCField, for forms and the like.
* Small performance improvement: changed a few Readers/Streams to Buffered
* Small (not so) performance improvement : changed a few fields in the lexer to static, so that the software is faster
	when building a lot of lexers (typically, in dictionaries and the like).
