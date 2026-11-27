package jsesh.jseshtemplates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TemplateParserTest {

    @Test
    void headerGivesNameDescriptionAndDocument() {
        var parsed = TemplateParser.parse("+bMy template+s-!\n+lFirst.+s-!\n+lSecond.+s-!\n+bEND+s-!\nA1-B1-!\n");
        assertEquals("My template", parsed.name());
        assertEquals("First.\nSecond.", parsed.description());
        assertEquals("A1-B1-!\n", parsed.document());
    }

    @Test
    void documentPreferencesAreKept() {
        String preferences = "++JSesh_Info 2.0 +s\r\n++JSesh_layout columns +s\r\n";
        var parsed = TemplateParser.parse("﻿" + preferences + "+bName+s-!\r\n+bEND+s-!\r\nA1\r\n");
        assertEquals("Name", parsed.name());
        assertEquals("", parsed.description());
        assertEquals(preferences + "A1\r\n", parsed.document());
    }

    @Test
    void headerCanUseEscapesCommentsSeveralScriptsAndBreakOptions() {
        var parsed = TemplateParser.parse(
                "+lA \\+ B +iitalic\nnext+s-++ not shown +s-![align=center]\n+lthe END+s-!!\n+lEND+s!=150%\n+lbody+s");
        assertEquals("A + B italic next", parsed.name());
        assertEquals("the END", parsed.description());
        assertEquals("+lbody+s", parsed.document());
    }

    @Test
    void contentMayBeEmpty() {
        assertEquals("", TemplateParser.parse("+bName+s-!\n+bEND+s").document());
    }

    @Test
    void endMarkerIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> TemplateParser.parse("+bName+s-!\nA1-!\n"));
    }

    @Test
    void nameIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> TemplateParser.parse("+bEND+s-!\nA1-!\n"));
    }
}
