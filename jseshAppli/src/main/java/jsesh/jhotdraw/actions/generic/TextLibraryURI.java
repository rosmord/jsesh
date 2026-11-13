/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.generic;

import java.net.URI;
import java.net.URISyntaxException;

/// Pseudo-URIs designating a text of the bundled JSesh text library, such as
/// `library:/jsesh-texts/Sinuhe.gly`.
///
/// A view loaded from such a URI is clean and has no associated file.
public final class TextLibraryURI {

    public static final String SCHEME = "library";

    private TextLibraryURI() {
    }

    /// @param libraryPath path of the text in the library, e.g. `jsesh-texts/Sinuhe.gly`.
    public static URI forLibraryPath(String libraryPath) {
        try {
            return new URI(SCHEME, null, "/" + libraryPath, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /// @return the library path (without leading slash) designated by a library URI.
    public static String toLibraryPath(URI uri) {
        String path = uri.getPath();
        if (!SCHEME.equals(uri.getScheme()) || path == null || path.length() < 2) {
            throw new IllegalArgumentException("Not a text library URI: " + uri);
        }
        return path.substring(1);
    }
}
