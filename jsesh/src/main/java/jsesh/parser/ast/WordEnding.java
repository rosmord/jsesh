/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.parser.ast;

/**
 * Whether a sign is followed by a word-end ({@code \}) or sentence-end
 * marker, as parsed.
 *
 * @author rosmord
 */
public enum WordEnding {
    NONE,
    WORD_END,
    SENTENCE_END
}
