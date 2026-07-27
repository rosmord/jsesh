*Released 2013/02/27*

6.3.2 fixes a bug in the JSesh installer on Windows 7 64 bytes, and adds a few improvements too:

  * the last line of a justified text is left as is if the line is too short.
  * inter-quadrat skips are now parts of document preferences and they can be changed.

  
6.3.1 fixes a small bug in 6.3.0, which it replaces.

bug fix : user information added with the SignInfo application was not used in the user interface. Now it is.
(if you have downloaded 6.3.0, and if you don't use SignInfo, you can keep 6.3).

JSesh 6.3.0 includes a few improvements:

  * Better behaviour when importing Winglyph files : if JSesh doesn't understand a line in a winglyph file, it will now read the rest of the text, and display the MdC line as plain text. Previous behaviour was to print an error message and stop reading.
  * Bug fix in the palette behaviour (in some cases, the palette displayed the same sign a number of times)
  * Bug fix:  WYSIWYG copy method did not always use "as one picture" to export the picture.
  * Help menu (opens the JSesh documentation web site)
  * And, most important, a basic justification system :

When you type a text in JSesh, if you want *all lines* to be the same length, simply select menu "Files/Format/Justify Text".
Here is an example of the result with Louvre C14 Stela:

This simple justification system will be replaced by something more refined in the future. This is the reason why *justification preferences are not saved with texts*. 
A feature (which might not be that good, but I like it) is that lines which are too short will be centered instead of justified.
