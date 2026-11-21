package jsesh.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import jsesh.io.document.MDCDocumentReader;
import jsesh.io.document.MDCDocumentWriter;
import jsesh.io.mdc.MdCModelWriter;
import jsesh.model.MDCPosition;
import jsesh.model.TopItem;
import jsesh.model.tools.GroupExploder;

/// The sign ids (`\sid`): who keeps an id, who gets a new one.
class SignIdTest {

    private static HieroglyphicTextModel model(String mdc, SignIdManager.Policy policy) throws Exception {
        HieroglyphicTextModel m = new HieroglyphicTextModel();
        m.setMDCCode(mdc);
        m.getSignIds().setPolicy(policy);
        if (policy == SignIdManager.Policy.NUMBERED) {
            m.getSignIds().setFromHeader(0);
            m.getSignIds().adopt(m.getHieroglyphicText());
        }
        return m;
    }

    private static String mdc(HieroglyphicTextModel m) {
        return new MdCModelWriter().toMdC(m.getHieroglyphicText());
    }

    private static List<Integer> ids(String mdc) {
        Matcher matcher = Pattern.compile("\\\\sid(\\d+)").matcher(mdc);
        List<Integer> result = new java.util.ArrayList<>();
        while (matcher.find()) {
            result.add(Integer.parseInt(matcher.group(1)));
        }
        return result;
    }

    private static MDCPosition end(HieroglyphicTextModel m) {
        return m.getLastPosition();
    }

    @Test
    void explodeKeepsIds() throws Exception {
        HieroglyphicTextModel m = model("p\\sid2*t\\sid3:pt\\sid4", SignIdManager.Policy.NUMBERED);
        MDCPosition first = m.buildFirstPosition();
        List<TopItem> selection = m.getTopItemsBetween(first, end(m));
        List<TopItem> exploded = new GroupExploder().explode(selection.get(0));
        m.replaceElement(first, end(m), exploded);
        assertEquals("p\\sid2*t\\sid3-pt\\sid4", mdc(m));
        assertEquals(4, m.getSignIds().getMaxId());
    }

    @Test
    void insertionGetsFreshIds() throws Exception {
        HieroglyphicTextModel m = model("p\\sid2-t\\sid3", SignIdManager.Policy.NUMBERED);
        // paste a copy of the first sign, twice
        List<TopItem> copy1 = m.getTopItemsBetween(m.buildFirstPosition(), m.buildPosition(1));
        List<TopItem> copy2 = m.getTopItemsBetween(m.buildFirstPosition(), m.buildPosition(1));
        m.insertElementsAt(end(m), copy1);
        m.insertElementsAt(end(m), copy2);
        assertEquals("p\\sid2-t\\sid3-p\\sid4-p\\sid5", mdc(m));
    }

    @Test
    void insertionNeverReusesAnIdAfterUndo() throws Exception {
        HieroglyphicTextModel m = model("p\\sid2", SignIdManager.Policy.NUMBERED);
        m.insertMDCText(end(m), "t");
        assertEquals("p\\sid2-t\\sid3", mdc(m));
        m.undo();
        m.insertMDCText(end(m), "t");
        assertEquals("p\\sid2-t\\sid4", mdc(m));
        m.undo();
        m.redo();
        assertEquals("p\\sid2-t\\sid4", mdc(m));
        assertEquals(4, m.getSignIds().getMaxId());
    }

    @Test
    void editedLineKeepsValidIdsAndRenumbersTheOthers() throws Exception {
        HieroglyphicTextModel m = model("p\\sid2-t\\sid3-i\\sid4", SignIdManager.Policy.NUMBERED);
        // A typo corrected (p -> n), a sign duplicated (t), a new sign (w).
        m.replaceWithMDCText(m.buildFirstPosition(), end(m), "n\\sid2-t\\sid3-t\\sid3-w-i\\sid4");
        assertEquals("n\\sid2-t\\sid3-t\\sid5-w\\sid6-i\\sid4", mdc(m));
    }

    @Test
    void anIdOfAnotherPlaceIsNotAcceptedInAReplacement() throws Exception {
        HieroglyphicTextModel m = model("p\\sid2-t\\sid3-i\\sid4", SignIdManager.Policy.NUMBERED);
        // replace "p" by a line which quotes the id of "i"
        m.replaceWithMDCText(m.buildFirstPosition(), m.buildPosition(1), "w\\sid4");
        assertEquals("w\\sid5-t\\sid3-i\\sid4", mdc(m));
    }

    @Test
    void stripPolicyRemovesIds() throws Exception {
        HieroglyphicTextModel m = model("p-t", SignIdManager.Policy.STRIP);
        m.replaceWithMDCText(m.buildFirstPosition(), end(m), "p\\sid2-t\\sid3\\red");
        assertEquals("p-t\\red", mdc(m));
    }

    @Test
    void unmanagedLeavesTheTextAlone() throws Exception {
        HieroglyphicTextModel m = model("p-t", SignIdManager.Policy.UNMANAGED);
        m.replaceWithMDCText(m.buildFirstPosition(), end(m), "p\\sid2-t\\sid2");
        assertEquals("p\\sid2-t\\sid2", mdc(m));
    }

    @Test
    void onlyRealSignsAreNumbered() throws Exception {
        HieroglyphicTextModel m = model("", SignIdManager.Policy.NUMBERED);
        m.insertMDCText(m.buildFirstPosition(), "p-o-\"abc\"-<-t-i->-!-t:(i*w)-..");
        String result = mdc(m);
        // p, t (cartouche), i (cartouche), t, i, w: six signs; "o", the small text and the spaces get nothing.
        assertEquals(6, ids(result).size(), result);
        assertEquals(6, ids(result).stream().distinct().count(), result);
    }

    @Test
    void numberSignsThenRemoveThemIsUndoable() throws Exception {
        HieroglyphicTextModel m = model("p-t:i-p", SignIdManager.Policy.UNMANAGED);
        m.numberSigns();
        assertEquals(4, ids(mdc(m)).stream().distinct().count());
        assertTrue(m.getSignIds().isNumbered());
        m.removeSignIds();
        assertEquals("p-t:i-p", mdc(m));
        assertFalse(m.getSignIds().isNumbered());
        m.undo();
        assertEquals(4, ids(mdc(m)).stream().distinct().count());
    }

    @Test
    void headerRoundTrip() throws Exception {
        MDCDocument doc = new MDCDocument();
        doc.getHieroglyphicTextModel().setMDCCode("p-t");
        doc.getHieroglyphicTextModel().numberSigns();
        String written = new MDCDocumentWriter().toMdC(doc);
        assertTrue(written.contains("++JSesh_max_sign_id 2 +s"), written);

        MDCDocument read = new MDCDocumentReader().readString(written, new File("x.gly"));
        SignIdManager ids = read.getHieroglyphicTextModel().getSignIds();
        assertTrue(ids.isNumbered());
        assertEquals(2, ids.getMaxId());
    }

    @Test
    void numberedWithoutAnyIdIsKeptByTheHeader() throws Exception {
        MDCDocument doc = new MDCDocument();
        doc.getHieroglyphicTextModel().getSignIds().setPolicy(SignIdManager.Policy.NUMBERED);
        MDCDocumentWriter writer = new MDCDocumentWriter();
        writer.prepareForSaving(doc);
        assertEquals(jsesh.model.constants.Dialect.JSESH1, doc.getDialect());
        String written = writer.toMdC(doc);
        assertTrue(written.contains("++JSesh_max_sign_id 0 +s"), written);
        MDCDocument read = new MDCDocumentReader().readString(written, new File("x.gly"));
        assertTrue(read.getHieroglyphicTextModel().getSignIds().isNumbered());
    }

    @Test
    void loadingFixesDuplicatesAndRaisesTheMax() throws Exception {
        String file = "++JSesh_Info 2.0 +s\n++JSesh_max_sign_id 3 +s\np\\sid2-t\\sid7-i\\sid7";
        MDCDocument read = new MDCDocumentReader().readString(file, new File("x.gly"));
        assertEquals("p\\sid2-t\\sid7-i\\sid8", mdc(read.getHieroglyphicTextModel()));
        assertEquals(8, read.getHieroglyphicTextModel().getSignIds().getMaxId());
    }

    @Test
    void aDocumentWithoutIdsStripsThem() throws Exception {
        MDCDocument read = new MDCDocumentReader().readString("p-t\n", new File("x.gly"));
        HieroglyphicTextModel m = read.getHieroglyphicTextModel();
        assertEquals(SignIdManager.Policy.STRIP, m.getSignIds().getPolicy());
        m.replaceWithMDCText(m.buildFirstPosition(), m.getLastPosition(), "p\\sid3");
        assertEquals("p", mdc(m));
    }
}
