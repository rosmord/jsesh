*Released 2020/06/09*

JSesh 7.4.2 is out !

(If you have problems installing it, please refer to [this post](https://jsesh.qenherkhopeshef.org/fr/news/news20200316)).

It mainly improves the search system with new functions.

Here are a few examples.

## Search in non-hieroglyphic texts

Search is no longer restricted to the hieroglyphs. If you have typed translation
or transliterations, you can search them too...

Select *search in alphabetic texts*...

Then type your text (search will disregard case) :

## Wildcard searches

You have now access to more powerful searches, with the `*` and `[...]` operators 
often found in search systems.

- `*` allows you to search for sequences of non-strictly consecutive signs. For instance, "ir`*`t"
  will search for sequences of signs containing ir, then possibly a number of other signs, then t.
- the length of the sequence can be limited (if it's not, you could find a ir near the beginning of a text and a t near the end)

To introduce the `*` in the search, use the corresponding button (indicated by a red arrow in the following figure).

In this example, the result length is limited to 4 signs. It includes both the ir and the t, meaning there can be 0, 1 or 2 signs between them. A limit of 0 is equivalent to "no limit".

- the `[...]` allows you to search for multiple possibilities. For instance, "p`[`w-W`]`" will search both for 
p-w and for p-W.

## Variants in searches

If you select "Extended variants", search result will include texts which contain any variant of your signs **as recorded in JSesh**.

As w and W are considered as variants, the following search will find occurrences of both 
p-w and for p-W too.

As of today, the definition of variants in JSesh lacks a bit of coherence (and data!). Don't expect 
very reliable results.

## Improvements to the hieroglyphic palette

The `show all` box in the hieroglyphic palette has been modified. Now, it will 
be used to hide all signs which are considered as variants of a main signs.

The "A" category with all signs displayed:

The "A" category with variants hidden:

Note that you can always access the variants of a sign using the button labelled 

"m-G17A" in the bottom left of the palette.

## Limitations

- the system should have an option to take line skips into account. It's not
 the case; line skips are ignored. It's nice if you want to find text which spans multiple lines,
  but in some cases, you would like to limit the search to line content.

- the variant system is completely dependant on the current information about signs in JSesh. And it's very 
  lacunar (it's hand-made !)

- variant search could be a bit more precise. Currently, there are two levels of searches: exact, and "with variants". 
  a limitation to "full variants" would be useful - using only signs which are completely equivalent.

- the bottom margin for the search field is bad: the bottom of the signs is hidden.
