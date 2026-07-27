*Released 2012/09/14*

JSesh 6.0.0 is out !

The main feature, along with small fixes, etc. is a "glossary editor". Select a few groups, open the glossary editor, give a code to the group, and then, access the group simply by typing the code.

The code can be anything that looks like manuel de codage, even a code already used elsewhere: the system uses the "space" key to cycle between all possibilities.

As a side effect, the grouping commands like ":" and "*" works in a more natural way.

Note that 6.0.0, being a 0.0 version, needs some testing. Keep using 5.8 if you are finishing an article, a thesis or anything of value. Even if it doesn't look like it, the changes are not so small. This being said, I have found this new version to respond well.

Another new feature is that you don't need to type the gardiner code with uppercase anymore. If you type aa1 or AA1, you 
will get the Aa1 sign anyway, for instance.

####Exempli gratia

Suppose you would like to be able to type the  article pꜣ in its hieratic form, which is G41-A1, simply by typing "pA". Here is what you need to do:

1.  type the text for the group you want.
2.  select it, and call "Edit/Add Selection to glossary"

3. the glossary editor window opens, and all you need to do is to type the code you want to use, and then push the "add" button.
4. as soon as the group has been added, you can insert it by typing its code. In this precise case, as we used "pA" as code, and "pA" allows to type G41 and G40, the space bar will allow you to cycle through all solutions.

#### Complements
1. you can remove entries by clicking the "remove" button which follows them in the glossary editor.
2. you can open or close the glossary editor by using the "Window/Glossary" menu entry.

#### Issues
Known issues are :
*   the current presentation of the glossary editor needs to be polished a bit.
*   when re-reading a pdf export in JSesh, the presentation choice (in particular "encapsulated pdf") is lost, which is a bit annoying for seamless editing of pdf files.
