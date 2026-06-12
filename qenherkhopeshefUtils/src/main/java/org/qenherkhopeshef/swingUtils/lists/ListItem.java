package org.qenherkhopeshef.swingUtils.lists;


/**
 * Polymorphic objects which can be added to JLists models, and handle both String content and actual content.
 * 
 * <p> The idea is typically to add a String as the first entry in the list, and actual data for the rest of the entries,
 * but to handle it in a typesafe way.
 * <p> It's useful even if the list contains only Strings, as it allows to distinguish between actual content and "placeholder" content.
 */
public sealed interface ListItem<T> permits DataListItem, StringListItem{
    

}
