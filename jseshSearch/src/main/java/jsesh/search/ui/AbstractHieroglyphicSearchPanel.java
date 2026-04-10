package jsesh.search.ui;

import javax.swing.JPanel;

import jsesh.editor.JMDCField;
import jsesh.hieroglyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.ResourcesHieroglyphicShapeRepository;
import jsesh.search.wildcard.WildCardQuery;

/**
 * A JPanel which contains hieroglyphic search field.
 * Such a panel needs to have access to extended fonts, which include, in particular, specific signs for search structures.
 * @author rosmord
 */
@SuppressWarnings("serial")
public abstract class AbstractHieroglyphicSearchPanel extends JPanel {

	/**
	 * Fonts specific to the search panel.
	 */
    private static final String FONT_PATH = "/jsesh/search/wildcard";

	protected HieroglyphShapeRepository fontManager;
	
	
	
    public AbstractHieroglyphicSearchPanel(HieroglyphShapeRepository fontManager) {
		super();
		setupFont(fontManager);
	}

	/**
     * Sets the font manager, and ensure  the additional signs for wildcards (*, [ and ]) are known.
     * @param fontManager : an original font manager.
     */
    private final void setupFont(HieroglyphShapeRepository fontManager) {    	
        if (fontManager.hasCode(WildCardQuery.QUERY_SKIP) ) {
        	this.fontManager = fontManager;
        } else {
        	CompositeHieroglyphShapeRepository compositeManager = new CompositeHieroglyphShapeRepository();
        	compositeManager.addHieroglyphicFontManager(fontManager);
        	compositeManager.addHieroglyphicFontManager(new ResourcesHieroglyphicShapeRepository(FONT_PATH));
        	this.fontManager = compositeManager;
        } 
    }
    
    protected HieroglyphShapeRepository getFontManager() {
		return fontManager;
	}
	
}
