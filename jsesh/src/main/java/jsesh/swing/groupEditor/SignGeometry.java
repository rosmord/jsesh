/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.swing.groupEditor;

import java.util.Objects;
import jsesh.mdc.model.Hieroglyph;

/**
 * Placement and rotation information about a sign.
 * <p> Represents the scaling, position and rotation of a given sign.
 * <p> This is an immutable value class.
 * @author rosmord
 */
public final class SignGeometry {
    /**
     * Position x
     */
    private final int x;
    
    /**
     * Position y
     */
    private final int y;
    
    /**
     * scale (in percent)
     */
    private final int scale;
    
    /**
     * Angle (in degrees)
     */
    private final int angle;

    public SignGeometry(int x, int y, int scale, int angle) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.angle = angle;
    }

    /**
     * Initialize a geometry from information in a sign.
     * @param sign 
     */
    public SignGeometry(Hieroglyph sign) {
        this.x = sign.getX();
        this.y = sign.getY();
        this.scale = sign.getRelativeSize();
        this.angle = sign.getAngle();
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

    public int getAngle() {
        return angle;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof SignGeometry) {
            SignGeometry other = (SignGeometry) o;
            return this.x == other.x
                    && this.y == other.y
                    && this.scale == other.scale
                    && this.angle == other.angle;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(x,y,scale, angle);
    }
    

    public SignGeometry withX(int newX) {
         return new SignGeometry(newX, y, scale, angle);
    }

    public SignGeometry withY(int newY) {
        return new SignGeometry(x, newY, scale, angle);
    }

    public SignGeometry withScale(int newScale) {
        return new SignGeometry(x, y, newScale, angle);
    }

    public SignGeometry withAngle(int newAngle) {
        return new SignGeometry(x, y, scale, newAngle);
    }
    
    public void applyTo(Hieroglyph h) {
        h.setAngle(angle);
        h.setExplicitPosition(x, y, scale);
    }
    
}
