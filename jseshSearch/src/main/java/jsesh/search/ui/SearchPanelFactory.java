/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2016) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est un programme informatique servant à mettre en place 
 * une base de données linguistique pour l'égyptien ancien.

 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL-C telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.cecill.info".

 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.

 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant 
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à 
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement, 
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité. 

 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez 
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package jsesh.search.ui;

import jsesh.search.clientApi.CorpusSearchTarget;
import jsesh.search.clientApi.SearchTarget;

/**
 * Factory class for creating search panels.
 *
 * <p> Note to self : factories are nice, but they don't work well 
 * with an IDE editor.
 * 
 * <p> Basically, we want graphical components with default constructors, plus
 * setters to choose the actual behaviour.
 * @author rosmord
 */
public class SearchPanelFactory {

    /**
     * No instance of this class shall be created.
     */
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
