package jsesh.utilitysoftwares.signinfoeditor.ui;

import javax.swing.ImageIcon;

/**
 * Factory for icons.
 */
public class IconFactory {
    

    private static ImageIcon buildIcon(String iconName) {
        String path = "/jsesh/utilitysoftwares/signinfoeditor/icons/" + iconName + ".png";
        return new ImageIcon(IconFactory.class.getResource(path));
    }

    public static ImageIcon getAddIcon() {
        return buildIcon("edit_add");
    }

    public static ImageIcon getRemoveIcon() {
        return buildIcon("edit_remove");
    }

    public static ImageIcon getPreviousIcon() {
        return buildIcon("previous");
    }

    public static ImageIcon getNextIcon() {
        return buildIcon("next");
    }
}
