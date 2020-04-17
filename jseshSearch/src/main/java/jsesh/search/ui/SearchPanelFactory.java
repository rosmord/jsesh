/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.ui;

import jsesh.search.clientApi.CorpusSearchTarget;
import jsesh.search.clientApi.SearchTarget;

/**
 * Factory class for creating search panels.
 *
 * @author rosmord
 */
public class SearchPanelFactory {

    private SearchPanelFactory() {
    }

    /**
     * Create and returns a simple panel which allows wildcards searches.
     *
     * @param target
     * @return a JWildcardPanel
     */
    public static JWildcardPanel createWildCardPanel(SearchTarget target) {
        return new JWildcardPanel(target);
    }

    /**
     * Create and returns a simple panel which allows wildcards searches. The
     * panel won't have internal margins, which will allow an easier embedding
     * in a larger panel.
     *
     * @param target
     * @return a JWildcardPanel
     */
    public static JWildcardPanel createWildCardPanelForEmbedding(SearchTarget target) {
        return new JWildcardPanel(target, false);
    }

    public static JSearchFolderPanel createSearchFolderPanel(CorpusSearchTarget corpusSearchTarget) {
        SearchFolderControl control = new SearchFolderControl(corpusSearchTarget);
        return control.getPanel();
    }

}
