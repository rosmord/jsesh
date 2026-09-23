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

        int classOfI = classifier.classOf('i');
        int classOfF = classifier.classOf('f');
        int classOfA = classifier.classOf('a');

        assertNotEquals(classOfI, classOfF);
        assertNotEquals(classOfA, classOfI);
        assertNotEquals(classOfA, classOfF);
        assertEquals(classOfA, classifier.classOf('b'), "plain letters away from i/f share a class");
        assertEquals(Set.of(classOfI), classifier.classesOf(i));
        assertEquals(Set.of(classOfF), classifier.classesOf(f));
        assertTrue(classifier.classesOf(letters).containsAll(Set.of(classOfA, classOfI, classOfF)));
        assertTrue(classifier.classCount() < 10, "far fewer classes than code points in [a-z]");
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
