package jsesh.mdc.utils;

import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.*;

import java.util.ArrayList;
import java.util.List;

public class VerticallyCenteredGrouper {

    /**
     * Center the original item, if possible.
     * If not, will return the original item.
     * <strong>This method is supposed to work on copies of items.</strong>
     * @param original
     * @return the centered item.
     */
    public TopItem centerText(TopItem original) {
        AuxCenterer visitor = new AuxCenterer();
        original.accept(visitor);
        TopItem result = visitor.getCenteredTopItem();
        if (result == null)
            return original;
        else
            return result;
    }

    private class AuxCenterer extends ModelElementDeepAdapter {
        List<HBox> hboxes = new ArrayList<>();

        @Override
        public void visitCadrat(Cadrat c) {
            for (int i = 0; i < c.getNumberOfHBoxes(); i++) {
                hboxes.add(c.getHBox(i));
            }
        }

        /**
         * Returns a centered top item, or null if nothing could be done.
         * @return
         */
        public TopItem getCenteredTopItem() {
            if (hboxes.isEmpty()) return null;
            else {
                Cadrat c = new Cadrat();
                c.addHBox(createEmptyBox());
                for (HBox box : hboxes) {
                    c.addHBox(box);
                }
                c.addHBox(createEmptyBox());
                return c;
            }
        }

        private HBox createEmptyBox() {
            HBox box = new HBox();
            Hieroglyph halfSpace = new Hieroglyph(SymbolCodes.HALFSPACE);
            halfSpace.setRelativeSize(1);
            box.addHorizontalListElement(halfSpace);
            return  box;
        }
    }
}
