/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2016) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est un programme informatique servant à mettre en place 
 * une base de données linguistique pour l'égyptien ancien.

 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence [CeCILL|CeCILL-B|CeCILL-C] telle que diffusée par le CEA, le CNRS et l'INRIA 
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
package jsesh.jhotdraw.dialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import jsesh.editor.JMDCField;
import jsesh.jhotdraw.viewClass.JSeshView;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.utils.MDCCodeExtractor;
import jsesh.search.MdCSearchQuery;
import jsesh.search.QuadrantSearchQuery;
import jsesh.search.SignStringSearchQuery;
import net.miginfocom.swing.MigLayout;
import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

/**
 * Search Folder Panel for JSesh.
 * (Currently, the logic for the search has been added to the GUI object.
 * I'm deeply ashamed).
 * @author rosmord
 */
public final class JSearchFolderPanel extends JFrame {

    private final Application application;
    JTextField folderField;
    JButton chooseFolderButton;    
    JMDCField mDCField;
    JCheckBox searchGlyphsSearchBox;
    JButton searchButton;    
    JTable resultTable;

    public JSearchFolderPanel(Application application) {
        this.application = application;
        this.mDCField = new JMDCField();
        this.searchGlyphsSearchBox = new JCheckBox("search glyphs sequence");
        this.searchGlyphsSearchBox.setSelected(true);
        this.searchButton = new JButton("search");

        this.setLayout(new MigLayout());
        this.add(mDCField, "span 2,wrap");
        this.add(searchGlyphsSearchBox, "span 2,wrap");
        this.add(searchButton, "");

        this.pack();
        searchButton.addActionListener(e -> doSearch());
    }

    public void startSearch() {
        setVisible(true);
    }

    public MdCSearchQuery getQuery() {
        return null;
    }

    private Stream<String> searchInPath(Path p) {
        
        View view = application.getActiveView();
        if (view != null) {
            JSeshView jSeshView = (JSeshView) view;
            try {
                MdCSearchQuery query;
                if (searchGlyphsSearchBox.isSelected()) {
                    List<String> l = new MDCCodeExtractor().getCodesAsList(mDCField.getMDCText());
                    query = new SignStringSearchQuery(l);
                    jSeshView.doSearch(query);
                } else {
                    query= new QuadrantSearchQuery(mDCField.getHieroglyphicTextModel().getModel());
                    jSeshView.doSearch(query);
                }
            } catch (MDCSyntaxError e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void doSearch() {
        List result;
        Path path= null;
        List<String> l = Files.walk(path).flatMap(p -> searchInPath(p)).collect(Collectors.toList());        
    }


}
