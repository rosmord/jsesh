/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.glossary;

import java.util.EventObject;

/**
 *
 * @author rosmord
 */
@SuppressWarnings("serial")
public class GlossaryEntryRemoved extends EventObject {
    private final GlossaryEntry entry;

    
    public GlossaryEntryRemoved(Object source, GlossaryEntry entry) {
        super(source);
        this.entry= entry;
    }

    public GlossaryEntry getEntry() {
        return entry;
    }
    
    
    
}
