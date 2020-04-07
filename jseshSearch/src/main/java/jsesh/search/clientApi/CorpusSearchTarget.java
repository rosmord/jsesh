/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.clientApi;

/**
 *
 * @author rosmord
 */
public interface CorpusSearchTarget {
    /**
     * Display a search result in a particular file.
     * @param hit 
     */
    void showCorpusSearchHit(final CorpusSearchHit hit);
}
