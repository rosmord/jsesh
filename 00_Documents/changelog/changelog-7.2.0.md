*Released 2018/05/31*

JSesh 7.2.0 is out !

It adds a new (and long awaited) function : search in texts. You can search 
for a *sequence of glyphs* or for a particular quadrat.

*This release of JSesh 7.2 is dedicated to the memory of [Vincent Euverte](/page/vincent_euverte).*

What's new: 
- search function - both in the current text and in a *whole folder*. If you search on your account it
will look at *all* your "gly" files !
- lots of sign values added (from Leitz, *Quellentexte zur ägyptischen Religion*)
- small in the sign list
- Major clean-up of the texts included with JSesh. I have given them better names, 
  and added a (very) short notice to each of them. Some of those texts were typed long time ago, for tksesh
  or even for HieroTeX (around 1996 or 1997 for the earliests - blimey, I'm old).
- fixed the C303 sign, which was wrong (it's Neith breastfeeding two crocodiles, 
  they were mistaken as arms).
- add cursor in empty texts - its absence tended to disturb users.
- *triple-click* will now select the current line
- Export function now work even if no text is selected. They will select the 
  current page in most cases, except for PDF quick export, which will export the current line.
- fix for screens with very high resolution. Swing, the graphical system 
used by JSesh, is a bit old and doesn't perform well with very high resolution 
screens. Basically, the fonts and icons used to be way too small. Now, you will 
have normal fonts (at the cost of the pretty, Mac looking look and feel 
JSesh had. The library I used for that is no longer maintained). You will also 
be able to choose the size of your icons.
- other bug fixes, for instance for units.

Please, regarding sign values: note that they are provided as a small help,
mainly for finding the signs. Serious documentation about the sign requires,
for instance, to distinguish usual values from extremely unusual ones. I don't 
have the time to do this systematically, and, when I do, I usually write a 
detailled rubric which goes in the sign description.
