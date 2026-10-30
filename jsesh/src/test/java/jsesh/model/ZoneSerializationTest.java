package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;

/// Copying to the clipboard serializes model elements: zones, inserts and
/// kernings must survive it.
class ZoneSerializationTest {

    @Test
    void zonesInsertsAndKerningsAreSerializable() throws Exception {
        String mdc = "a-%zone[width=1200,frame](b-c)-%insert[dir=ts,dw=20](O1,n:t)-p*%hkern*q-n:%vkern[a=2,b]:t";
        HieroglyphicText text = new MDCParserModelGenerator().parse(mdc);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(text.deepCopy());
        }
        Object back;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            back = in.readObject();
        }
        assertEquals(text.toString(), back.toString());
    }

    /// A long zone used to overflow the stack, because each element
    /// serialized its `next` sibling recursively.
    @Test
    void longZoneSerializesAndKeepsItsLinks() throws Exception {
        int n = 20000;
        String mdc = "%zone(" + "a-".repeat(n) + "b)";
        HieroglyphicText text = new MDCParserModelGenerator().parse(mdc);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(text.getTopItemAt(0).deepCopy());
        }
        Zone back;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            back = (Zone) in.readObject();
        }
        ModelElement content = back.getChildAt(0);
        assertEquals(back, content.getParent());
        assertEquals(n + 1, content.getNumberOfChildren());
        for (int i = 0; i < content.getNumberOfChildren(); i++) {
            ModelElement child = content.getChildAt(i);
            assertEquals(content, child.getParent());
            assertEquals(i == 0 ? null : content.getChildAt(i - 1), child.getPreviousSlibing());
            assertEquals(i == n ? null : content.getChildAt(i + 1), child.getNextSlibing());
        }
    }
}
