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
package jsesh.mdcDisplayer.drawingElements;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cartouche;
import jsesh.mdcDisplayer.drawingElements.cartouche.AbstractCartoucheDrawer;
import jsesh.mdcDisplayer.drawingElements.cartouche.EnclosureDrawer;
import jsesh.mdcDisplayer.drawingElements.cartouche.HwtDrawer;
import jsesh.mdcDisplayer.drawingElements.cartouche.NormalCartoucheDrawer;
import jsesh.mdcDisplayer.drawingElements.cartouche.SerekhDrawer;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.CartoucheSizeHelper;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Utility class for drawing cartouches and the like. This is not a fine example
 * of Object Oriented Design.
 * <p>
 * Now a front end for the classes in the cartouche package.
 *
 * @author rosmord
 */
public class CartoucheDrawerHelper {

    // preferences
    private final DrawingSpecification drawingSpecifications;
    // arguments
    private final TextDirection currentTextDirection;
    private final TextOrientation currentTextOrientation;
    // technical
    private final MDCView currentView;
    private final Graphics2D g;

    public CartoucheDrawerHelper(DrawingSpecification drawingSpecifications, TextDirection currentTextDirection, TextOrientation currentTextOrientation, MDCView currentView, Graphics2D g) {
        this.drawingSpecifications = drawingSpecifications;
        this.currentTextDirection = currentTextDirection;
        this.currentTextOrientation = currentTextOrientation;
        this.currentView = currentView;
        this.g = g;
    }

    public void visitHorizontalCartouche(Cartouche c) {
        AbstractCartoucheDrawer delegate = buildDelegate(c);
        delegate.drawHorizontal(c);
    }

    /**
     * Very close to the horizontal routine (except that left-to-right vs.
     * right-to-left are not very important there).
     *
     * @param c
     */
    public void visitVerticalCartouche(Cartouche c) {
        AbstractCartoucheDrawer delegate = buildDelegate(c);
        delegate.drawVertical(c);
    }

    private AbstractCartoucheDrawer buildDelegate(Cartouche c) {
        AbstractCartoucheDrawer delegate = null;
        switch (c.getType()) {
            case 'f':
            case 'F':
                delegate = new EnclosureDrawer(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);
                break;
            case 's':
                delegate = new SerekhDrawer(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);
                break;
            case 'h':
                delegate = new HwtDrawer(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);
                break;
            default:
                delegate = new NormalCartoucheDrawer(drawingSpecifications, currentTextDirection, currentTextOrientation, currentView, g);
        }
        return delegate;
    }

}
