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
package jsesh.graphics.export.rtf;

import jsesh.utils.EnumBase;

/**
 * Format preference for the pictures embedded in the RTF file.
 * <ul>
 * <li> DEFAULT : means "default" for this platform.</li>
 * <li> WMF : Windows metafile</li>
 * <li> EMF : Embedded metafile (use it if you can)</li>
 * <li> MACPICT: old mac vector format</li>
 * </ul>
 * @author rosmord
 */
public class RTFExportGraphicFormat extends EnumBase {
    
    public static final RTFExportGraphicFormat DEFAULT = new RTFExportGraphicFormat(0, "DEFAULT");
    public static final RTFExportGraphicFormat WMF = new RTFExportGraphicFormat(1, "WMF");
    public static final RTFExportGraphicFormat EMF = new RTFExportGraphicFormat(2, "EMF");
    public static final RTFExportGraphicFormat MACPICT = new RTFExportGraphicFormat(3, "MACPICT");

     /**
     * The list of formats for display.
     */
    public static final RTFExportGraphicFormat[] GRAPHIC_FORMATS = {
        RTFExportGraphicFormat.DEFAULT, RTFExportGraphicFormat.MACPICT, RTFExportGraphicFormat.WMF, RTFExportGraphicFormat.EMF
    };
    /**
     * returns the format, using its id number to identify it..
     * @param format
     * @return
     */
    public static RTFExportGraphicFormat getMode(int format) {
        RTFExportGraphicFormat[] formats = {DEFAULT, WMF, EMF, MACPICT};
        return formats[format];
    }

    private RTFExportGraphicFormat(int id, String designation) {
        super(id, designation);
    }
    
}
