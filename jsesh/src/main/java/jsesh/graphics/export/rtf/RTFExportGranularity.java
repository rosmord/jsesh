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
 * Possible modes for RTF export.
 */
public class RTFExportGranularity extends EnumBase {

    public static final RTFExportGranularity ONE_LARGE_PICTURE = new RTFExportGranularity(0, "as one large picture");
    public static final RTFExportGranularity GROUPED_CADRATS = new RTFExportGranularity(1, "grouped cadrats");
    public static final RTFExportGranularity ONE_PICTURE_PER_CADRAT = new RTFExportGranularity(2, "one picture per cadrat");
    
    public static final RTFExportGranularity[] GRANULARITIES = new RTFExportGranularity[]{ONE_LARGE_PICTURE, GROUPED_CADRATS, ONE_PICTURE_PER_CADRAT};

    private RTFExportGranularity(int id, String designation) {
        super(id, designation);
    }

    /**
     * Returns the granularity associated with a given ID.
     * @param granularity
     * @return
     */
    public static RTFExportGranularity getGranularity(int granularity) {
        return GRANULARITIES[granularity];
    }
    
}
