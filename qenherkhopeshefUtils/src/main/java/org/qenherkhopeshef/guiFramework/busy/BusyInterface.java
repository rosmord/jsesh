package org.qenherkhopeshef.guiFramework.busy;

/**
 * Interface for objects which can manage the "buzy" state of some graphic component.
 * Ideally, the graphical interface should block controls (key and mouse),
 * and perform some kind of animation until busy is set to false.
 * 
 * <p> At some time, we will add a facility to send a completion percentage if possible<p>
 * 
 * <p> and maybe cancel ?</p>
 * <p> Note that all method here are performed on the EDT.</p>
 * @author rosmord
 *
 */
public interface BusyInterface {
	void setBuzy(boolean buzy);
	
	void setAdvance(int percent);
}
