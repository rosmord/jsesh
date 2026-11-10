/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.clipboard;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.datatransfer.DataFlavor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.document.embeddedsigns.EmbeddedSignImport;
import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.document.embeddedsigns.EmbeddedSignShapeRepository;
import jsesh.glyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.io.mdc.MdcTextHelper;
import jsesh.model.HieroglyphicText;
import jsesh.model.tools.SignCodeRenamer;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.ui.editor.JMDCEditorWorkflow;

/// Copying text with embedded signs, and pasting it in another document.
public class JSeshFragmentTest {

    private static final byte[] SVG_A = "<svg id='a'/>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SVG_B = "<svg id='b'/>".getBytes(StandardCharsets.UTF_8);

    private static final DataFlavor[] FLAVORS = { JSeshPasteFlavors.ListOfTopItemsFlavor, DataFlavor.stringFlavor };

    private static MDCModelTransferable transferable(String mdc, EmbeddedSignList signs) throws Exception {
        CompositeHieroglyphShapeRepository repository = new CompositeHieroglyphShapeRepository();
        repository.addRepository(new EmbeddedSignShapeRepository(signs));
        return new MDCModelTransferable(FLAVORS, MdcTextHelper.parse(mdc), null,
                new JSeshRenderContext(JSeshStyle.DEFAULT, repository));
    }

    @Test
    public void renamerIsSimultaneousAndSeesLigaturesAndCartouches() throws Exception {
        HieroglyphicText text = MdcTextHelper.parse("sign1-sign2&&A1-<-sign1:sign2->-A1");
        new SignCodeRenamer(Map.of("sign1", "sign2", "sign2", "sign3")).rename(List.of(text));
        String result = MdcTextHelper.toMdc(text);
        // The writer writes a ligature of two plain signs as "**".
        assertTrue(result.contains("sign2-sign3**A1"), result);
        assertTrue(result.contains("sign2:sign3"), result);
        assertFalse(result.contains("sign1"), result);
    }

    @Test
    public void textWithoutEmbeddedSignsIsAPlainListOfTopItems() throws Exception {
        EmbeddedSignList signs = new EmbeddedSignList();
        signs.add(SVG_A, null, null);
        MDCModelTransferable t = transferable("A1-G17", signs);
        assertEquals(List.of(FLAVORS), List.of(t.getTransferDataFlavors()));
        assertTrue(t.getTransferData(JSeshPasteFlavors.ListOfTopItemsFlavor) instanceof jsesh.model.ListOfTopItems);
    }

    @Test
    public void copyAndPasteInAnotherDocument() throws Exception {
        EmbeddedSignList source = new EmbeddedSignList();
        source.add(SVG_A, "A1", "a");   // sign1
        source.add(SVG_B, null, null);  // sign2
        source.add(SVG_A.clone(), null, null);  // sign3, not used
        MDCModelTransferable t = transferable("A1-sign1-sign2&&A1-<-sign1:sign2->", source);

        assertFalse(t.isDataFlavorSupported(JSeshPasteFlavors.ListOfTopItemsFlavor));
        assertTrue(t.isDataFlavorSupported(JSeshPasteFlavors.JSeshFragmentFlavor));
        assertTrue(t.isDataFlavorSupported(DataFlavor.stringFlavor));
        JSeshFragment fragment = serializedCopy(
                (JSeshFragment) t.getTransferData(JSeshPasteFlavors.JSeshFragmentFlavor));
        assertEquals(List.of("sign1", "sign2"), fragment.signs().stream().map(s -> s.code()).toList());

        // The target has the drawing of sign2 as its own sign1, and has used numbers up to 2.
        EmbeddedSignList target = new EmbeddedSignList();
        target.add(SVG_B, null, "mine");
        target.add(SVG_B.clone(), null, null);
        target.remove("sign2");
        JMDCEditorWorkflow workflow = new JMDCEditorWorkflow(new HieroglyphicTextModel(), code -> null);
        workflow.setMDCCode("sign1");
        workflow.cursorToEndOfLine();

        EmbeddedSignImport plan = fragment.adaptTo(target);
        assertTrue(workflow.insertElements(fragment.items()));
        plan.commit();

        assertEquals(Map.of("sign1", "sign3", "sign2", "sign1"), plan.renaming());
        String result = workflow.getMDCCode();
        assertTrue(result.startsWith("sign1-A1-sign3-sign1**A1-<-sign3:sign1->"), result);
        assertEquals(2, target.size());
        assertArrayEquals(SVG_A, target.get("sign3").orElseThrow().svg());
        assertEquals("A1", target.get("sign3").orElseThrow().variantOf());
        assertEquals("mine", target.get("sign1").orElseThrow().description());
    }

    private static JSeshFragment serializedCopy(JSeshFragment fragment) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(fragment);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (JSeshFragment) in.readObject();
        }
    }
}
