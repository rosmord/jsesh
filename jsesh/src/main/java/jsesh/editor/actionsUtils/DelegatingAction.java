/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.editor.actionsUtils;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

/**
 * An action whose behaviour is delegated to a specific object's method.
 * TODO : add a guarding mecanism (like "isEditable") to prevent accidents.
 * @author rosmord
 */
@SuppressWarnings("serial")
public class DelegatingAction extends AbstractAction {

    private Object delegate;
    private Method delegatingMethod;
    private Enabler enabler= null;

    /**
     * Create the action for a specific object and method.
     * All action properties (name, etc...) must be set
     * at some point afterwards.
     * @param delegate
     * @param methodName : the name of the method. Its signature is
     *  supposed to be void <em>methodName</em>()
     */
    public DelegatingAction(Object delegate, String methodName) {
        try {
            this.delegate = delegate;
            delegatingMethod = delegate.getClass().getMethod(methodName);
            if (delegate == null || delegatingMethod == null) {
                this.setEnabled(false);
            }
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(DelegatingAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(DelegatingAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    public void actionPerformed(ActionEvent e) {
        if (enabler != null && ! enabler.canDo())
            return;
        try {
            if (delegate != null && delegatingMethod != null)
                delegatingMethod.invoke(delegate);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DelegatingAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DelegatingAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(DelegatingAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setEnabler(Enabler enabler) {
        this.enabler = enabler;
    }

    public Enabler getEnabler() {
        return enabler;
    }

    
}
