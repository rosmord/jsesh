/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.RunningAnnotation;
import jsesh.parser.MDCParser;
import jsesh.parser.MDCSyntaxError;
import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHBox;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstOption;
import jsesh.parser.ast.AstOptionList;
import jsesh.parser.ast.AstRunningAnnotation;
import jsesh.sharedvocabulary.RunningAnnotationKind;

/// Running annotations (`%haplography[size=...]`, `%lacuna[size=...]`, and the
/// legacy `^-..-^`, `?-..-?`): AST, model, writing back, copying.
public class RunningAnnotationMdcTest {

    private HieroglyphicText parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private String write(HieroglyphicText text) {
        return new MdCModelWriter().toMdC(text);
    }

    private RunningAnnotation annotation(String mdc) throws MDCSyntaxError {
        return assertInstanceOf(RunningAnnotation.class, parse(mdc).getTopItemAt(1));
    }

    // ---- AST ----

    @Test
    public void bareKeywordGivesNoOptions() throws MDCSyntaxError {
        assertEquals(AstDocument.of(new AstRunningAnnotation(RunningAnnotationKind.HAPLOGRAPHY, AstOptionList.of())),
                new MDCParser().parse("%haplography"));
    }

    @Test
    public void optionsAreKeptInTheAst() throws MDCSyntaxError {
        AstDocument expected = AstDocument.of(
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("A1"))),
                new AstRunningAnnotation(RunningAnnotationKind.LACUNA,
                        AstOptionList.of(AstOption.of("size", "fill"))),
                AstCadrat.of(AstHBox.of(AstHieroglyph.of("B1"))));
        assertEquals(expected, new MDCParser().parse("A1-%lacuna[size=fill]-B1"));
    }

    @Test
    public void legacyFormGivesTheSameAstAsTheKeyword() throws MDCSyntaxError {
        assertEquals(new MDCParser().parse("A1-%haplography[size=200]-B1"),
                new MDCParser().parse("A1-^-..-..-^-B1"));
        assertEquals(new MDCParser().parse("A1-%lacuna[size=150]-B1"),
                new MDCParser().parse("A1-?-.-..-?-B1"));
    }

    // ---- Model ----

    @Test
    public void sizeIsReadInPercentsOfAQuadrat() throws MDCSyntaxError {
        RunningAnnotation a = annotation("A1-%haplography[size=250]-B1");
        assertEquals(RunningAnnotationKind.HAPLOGRAPHY, a.getKind());
        assertFalse(a.isFill());
        assertEquals(250, a.getPercent());
    }

    @Test
    public void fillIsRead() throws MDCSyntaxError {
        RunningAnnotation a = annotation("A1-%lacuna[size=fill]-B1");
        assertEquals(RunningAnnotationKind.LACUNA, a.getKind());
        assertTrue(a.isFill());
    }

    @Test
    public void aMissingOrInvalidSizeIsOneQuadrat() throws MDCSyntaxError {
        for (String mdc : new String[] { "A1-%haplography-B1", "A1-%haplography[size=0]-B1",
                "A1-%haplography[size=big]-B1", "A1-%haplography[other=3]-B1" }) {
            RunningAnnotation a = annotation(mdc);
            assertFalse(a.isFill(), mdc);
            assertEquals(RunningAnnotation.DEFAULT_PERCENT, a.getPercent(), mdc);
        }
    }

    @Test
    public void legacyFormsAreReadAsAnnotations() throws MDCSyntaxError {
        RunningAnnotation variant = annotation("A1-^-..-..-^-B1");
        assertEquals(RunningAnnotationKind.HAPLOGRAPHY, variant.getKind());
        assertEquals(200, variant.getPercent());
        RunningAnnotation lacuna = annotation("A1-?-..-..-..-?-B1");
        assertEquals(RunningAnnotationKind.LACUNA, lacuna.getKind());
        assertEquals(300, lacuna.getPercent());
    }

    @Test
    public void itIsATopItemBetweenTwoQuadrats() throws MDCSyntaxError {
        assertEquals(3, parse("A1-%lacuna[size=100]-B1").getNumberOfChildren());
    }

    @Test
    public void copiesAreEqual() throws MDCSyntaxError {
        RunningAnnotation a = annotation("A1-%haplography[size=250]-B1");
        assertTrue(a.equalsIgnoreId(a.deepCopy()));
        assertEquals(0, a.compareTo(a.deepCopy()));
        RunningAnnotation filling = annotation("A1-%haplography[size=fill]-B1");
        assertTrue(filling.equalsIgnoreId(filling.deepCopy()));
        assertTrue(filling.deepCopy().isFill());
        assertFalse(a.equalsIgnoreId(filling));
        assertFalse(a.equalsIgnoreId(annotation("A1-%lacuna[size=250]-B1")));
    }

    // ---- Writing ----

    @Test
    public void theSizeIsAlwaysWritten() throws MDCSyntaxError {
        assertEquals("A1-%haplography[size=100]-B1", write(parse("A1-%haplography-B1")).trim());
        assertEquals("A1-%lacuna[size=fill]-B1", write(parse("A1-%lacuna[size=fill]-B1")).trim());
    }

    @Test
    public void legacyFormsAreWrittenAsKeywords() throws MDCSyntaxError {
        assertEquals("A1-%haplography[size=200]-B1", write(parse("A1-^-..-..-^-B1")).trim());
        assertEquals("A1-%lacuna[size=50]-B1", write(parse("A1-?-.-?-B1")).trim());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "A1-%haplography[size=200]-B1",
        "A1-%lacuna[size=fill]-B1",
        "%haplography[size=fill]-%tab-A1",
        "A1-%tab-%lacuna[size=fill]-%tab-B1-!\nC1-%haplography[size=300]",
        "%zone[width=1000](A1-%haplography[size=fill]-B1)",
        "%zone[layout=columns](A1-%lacuna[size=150]-B1)",
    })
    public void roundTrip(String mdc) throws MDCSyntaxError {
        HieroglyphicText text = parse(mdc);
        String written = write(text).trim();
        assertEquals(mdc, written);
        assertTrue(text.equalsIgnoreId(parse(written)));
    }
}
