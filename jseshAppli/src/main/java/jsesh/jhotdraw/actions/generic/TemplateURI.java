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

/// Pseudo-URIs designating a template of the bundled JSesh templates, such as
/// `template:/jsesh-templates/Numbered%20lines.gly`.
///
/// A view loaded from such a URI holds a new document: it is clean and has no
/// associated file.
public final class TemplateURI {

    public static final String SCHEME = "template";

    private TemplateURI() {
    }

    /// @param templatePath path of the template in the library, e.g. `jsesh-templates/Numbered lines.gly`.
    public static URI forTemplatePath(String templatePath) {
        try {
            return new URI(SCHEME, null, "/" + templatePath, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /// @return the template path (without leading slash) designated by a template URI.
    public static String toTemplatePath(URI uri) {
        String path = uri.getPath();
        if (!SCHEME.equals(uri.getScheme()) || path == null || path.length() < 2) {
            throw new IllegalArgumentException("Not a template URI: " + uri);
        }
        return path.substring(1);
    }
}
