/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.datatypes;

/**
 * Simple String-oriented utilities.
 */
public class StringUtils {
    private StringUtils() {
        super();
    }

    /**
     * Removes any non-alphanumeric characters to avoid having hyphens in translitterations.
     * 
     * <p> When used to retrieve signs, the presence of hyphens is a problem, as 
     * they have a specific meaning in MdC. We will thus replace Hwt-Hr by HwtHr.
     * @param originalCode
     * @return the normalized phonetic code.
     */
    public static String removeHyphens(String originalCode) {
        return originalCode.replaceAll("[^\\p{IsLetter}\\p{IsDigit}]", "");
    }
}
