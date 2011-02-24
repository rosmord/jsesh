/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.editor.actionsUtils;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import jsesh.editor.actions.AppDefaultFactory;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;

/**
 * An action whose behaviour is delegated to a specific object's method.
 * TODO : add a guarding mecanism (like "isEditable") to prevent accidents.
 * @author rosmord
 */
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

    /**
     * Create a deletegating method and initialize its properties using the AppDefaultFactory and an action ID.
     * @param delegate
     * @param methodName a argumentless method's name.
     * @param ID
     */
    public DelegatingAction(Object delegate, String methodName, String ID) {
        this(delegate, methodName);
        AppDefaults appDefaults = AppDefaultFactory.getAppDefaults();
        BundledActionFiller.initActionProperties(this, ID, appDefaults);
    }

    /**
     * Create a deletegating method, getting all properties from an AppDefault object.
     * @param delegate the object on which the action will be called
     * @param methodName the method to call on "delegate"
     * @param ID the ID which identifies the action in the appDefault. e.g. if ID = edit.toto, the
     *  action name will be edit.toto.Name
     * @param appDefaults
     */
     public DelegatingAction(Object delegate, String methodName, String ID, AppDefaults appDefaults) {
        this(delegate, methodName);
        BundledActionFiller.initActionProperties(this, ID, appDefaults);
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
