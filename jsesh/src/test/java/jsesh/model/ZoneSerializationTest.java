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
}
