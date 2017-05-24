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
package jsesh.mdcDisplayer.draw;

import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jsesh.editor.JMDCEditor;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

/**
 * Demonstrate/test absolute positioning in a context where sign dimensions are
 * modified.
 *
 * @author rosmord
 */
public class ScaledAbsolutePlacementTest {

    JMDCEditor editor1 = new JMDCEditor();
    JMDCEditor editor2 = new JMDCEditor();
    JMDCEditor editor3 = new JMDCEditor();

    JFrame frame = new JFrame("testA - scaling");

    public ScaledAbsolutePlacementTest() {
        String mdc = "wD**t**x{{1057,554,100}}**wD{{1583,11,98}}**n{{9,787,100}}";
        DrawingSpecificationsImplementation drawingSpecification = new DrawingSpecificationsImplementation();
        drawingSpecification.setMaxCadratHeight(30);
        drawingSpecification.setStandardSignHeight(30);
        //drawingSpecification.setTextOrientation(TextOrientation.VERTICAL);
        editor1.setDrawingSpecifications(drawingSpecification);
        editor1.setMDCText(mdc);
        drawingSpecification = new DrawingSpecificationsImplementation();
        drawingSpecification.setMaxCadratHeight(30);
        editor2.setDrawingSpecifications(drawingSpecification);
        editor2.setMDCText(mdc);
        editor3.setMDCText(mdc);
        frame.setLayout(new FlowLayout());
        frame.add(editor1);
        frame.add(editor2);
        frame.add(editor3);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScaledAbsolutePlacementTest());
    }
}
