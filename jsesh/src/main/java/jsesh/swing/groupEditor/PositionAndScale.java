
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

/**
 * Simple DTO class.
 * @author rosmord
 */
public class PositionAndScale {
    private int x, y, scale;

    public PositionAndScale(int x, int y, int scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getScale() {
        return scale;
    }
    
    
}
