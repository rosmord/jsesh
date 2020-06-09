
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.hieroglyphs.resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import jsesh.hieroglyphs.data.io.SignDescriptionReader;

/**
 * Helper class to access various embedded resources about glyphs.
 *
 * @author rosmord
 */
public class HieroglyphResources {

    private HieroglyphResources() {
    }

    public static Reader getBasicGardinerCodes() throws UnsupportedEncodingException {
        return new InputStreamReader(HieroglyphResources.class
                .getResourceAsStream("basicGardinerCodes.txt"), "UTF-8");
    }

    public static InputStream getSignDescriptionDTD() {
        InputStream inputStream = HieroglyphResources.class.getResourceAsStream("sign_description.dtd");
        return inputStream;
    }
    
    public static InputStream getSignsDescriptionXML() {
        InputStream in1 = HieroglyphResources.class.getResourceAsStream(
                "signs_description.xml");
        return in1;
    }

}
