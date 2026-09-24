/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.anyCharacter;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.character;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.characterRange;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.mdwlexer.Expression.CharacterSet;

class CharacterClassifierTest {
    @Test
    void oneClassWhenNothingNeedsToBeDistinguished() {
        CharacterClassifier classifier = CharacterClassifier.forExpressions(List.of(anyCharacter()));

        assertEquals(1, classifier.classCount());
        assertEquals(0, classifier.classOf('a'));
        assertEquals(0, classifier.classOf(0));
        assertEquals(0, classifier.classOf(Character.MAX_CODE_POINT));
    }

    @Test
    void splitsOverlappingCharacterSetsIntoDisjointClasses() {
        // The example from the design notes: [a-z], 'i' and 'f' each need their own class,
        // besides whatever is left of [a-z] once 'i' and 'f' are set aside.
        CharacterSet letters = (CharacterSet) characterRange('a', 'z');
        CharacterSet i = (CharacterSet) character('i');
        CharacterSet f = (CharacterSet) character('f');
        CharacterClassifier classifier = CharacterClassifier.forExpressions(List.of(letters, i, f));

        assertEquals(7, classifier.classCount(), "seven classes expected");
        int classOfA = classifier.classOf('a');
        int classOfB = classifier.classOf('b');        
        int classOfF = classifier.classOf('f');
        int classOfG = classifier.classOf('g');
        int classOfI = classifier.classOf('i');        
        int classOfZ = classifier.classOf('z');
        int classOfSpace = classifier.classOf(' ');

        assertNotEquals(classOfI, classOfF);
        assertNotEquals(classOfA, classOfI);
        assertNotEquals(classOfA, classOfSpace);
        assertNotEquals(classOfA, classOfF);
        assertEquals(classOfA, classOfB, "plain letters away before f share a class");
        assertNotEquals(classOfA, classOfZ, "letters ranges are split.");
        assertEquals(Set.of(classOfI), classifier.classesOf(i));
        assertEquals(Set.of(classOfF), classifier.classesOf(f));        
        assertEquals(Set.of(classOfA, classOfF, classOfG, classOfI, classOfZ), classifier.classesOf(letters));
    }

    @Test
    void classesExactlyCoverEveryRangeOfTheOriginalSet() {
        CharacterSet vowels = (CharacterSet) ExpressionBuilder.oneOf("aeiou");
        CharacterClassifier classifier = CharacterClassifier.forExpressions(List.of(vowels));

        Set<Integer> classes = classifier.classesOf(vowels);
        for (int codepoint : "aeiou".codePoints().toArray()) {
            assertTrue(classes.contains(classifier.classOf(codepoint)));
        }
        assertTrue(!classes.contains(classifier.classOf('b')));
    }
}
