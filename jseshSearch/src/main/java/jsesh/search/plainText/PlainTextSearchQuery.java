/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.plainText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jsesh.editor.MdCSearchQuery;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.EmbeddedModelElement;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TopItemList;

/**
 * Search for non-hieroglyphic text in the MdC content.
 *
 * @author rosmord
 */
public class PlainTextSearchQuery implements MdCSearchQuery {

    private String searchText;

    public PlainTextSearchQuery(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public List<MDCPosition> doSearch(TopItemList items) {
        List<TextAndPosition> searchableText = buildSearchableText(items);
        List<MDCPosition> result = new ArrayList<>();

        for (TextAndPosition t : searchableText) {
            List<Integer> list = t.match(searchText);
            result.addAll(
                    list.stream()
                            .map(i -> new MDCPosition(items, i))
                            .collect(Collectors.toList()));
        }
        return result;
    }

    private List<TextAndPosition> buildSearchableText(TopItemList items) {
        ArrayList<TextAndPosition> result = new ArrayList<>();
        TextAndPosition.Builder currentBuilder = null;
        for (int pos = 0; pos < items.getNumberOfChildren(); pos++) {
            EmbeddedModelElement item = items.getChildAt(pos);
            SearchableTextBuilder visitor = new SearchableTextBuilder();
            item.accept(visitor);
            if (visitor.hasText()) {
                if (currentBuilder == null) {
                    currentBuilder = TextAndPosition.builder(pos);
                }
                currentBuilder.add(visitor.getText());
            } else {
                if (currentBuilder != null) {
                    result.add(currentBuilder.build());
                    currentBuilder = null;
                }
            }
        }
        if (currentBuilder != null)
            result.add(currentBuilder.build());
        return result;
    }
    
    private static class SearchableTextBuilder extends ModelElementDeepAdapter {

    StringBuilder builder = new StringBuilder();

    public boolean hasText() {
        return builder.length() != 0;
    }

    public String getText() {
        return builder.toString();
    }

    @Override
    public void visitAlphabeticText(AlphabeticText t) {
        builder.append(t.getText());
    }

    @Override
    public void visitSuperScript(Superscript s) {
        builder.append(s.getText());
    }

}

}
