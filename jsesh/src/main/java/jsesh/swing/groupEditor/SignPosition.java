
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import java.awt.geom.Point2D;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Sign position, as used in JSesh (i.e. relative to A1 size).
 *
 * @author rosmord
 */
public class SignPosition {

    private int x, y;

    public SignPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public SignPosition(DrawingSpecification specs, Point2D.Double position) {
        // Convert to integers :
        double unitSize = specs.getHieroglyphsDrawer()
                .getGroupUnitLength();
        x = (int) (position.getX() / unitSize);
        y = (int) (position.getY() / unitSize);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.x;
        hash = 83 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SignPosition other = (SignPosition) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

}
