package org.qenherkhopeshef.swingUtils;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.InputEvent;
import java.util.Locale;

import org.qenherkhopeshef.utils.PlatformDetection;

import javafx.application.Platform;

///
/// Facade hiding the GUI toolkit, so that headless mode can be used.
///
/// When a "true" toolkit is not available, we get a headless version thereof.
public interface PortableToolKit {

    /**
     * Create a temporary toolkit.
     * No need to keep it in memory, BTW.
     * @return a correct toolkit, depending on the headlessness of the environment.
     */
    public static PortableToolKit createToolkit() {
        if (GraphicsEnvironment.isHeadless()) {
            return new HeadlessToolKit();
        } else {
            return new SwingToolKit();
        }
    }

    /// Gets the screen size.
    /// 1024x768 is returned in headless mode.
    Dimension getScreenSize();

    /// Gets the menu shortcut key mask.
    /// This should be replaced, as `getMenuShortcutKeyMask` is deprecated ok Toolkit.
    @Deprecated 
    int getMenuShortcutKeyMask();

    /// get the system clipboard, or a dummy one if headless.    
    Clipboard getSystemClipboard();
}

/// A toolkit which doesn't depend on a live environment.
/// 
/// - shortcuts are computed relative to the current platform
/// - screen size is set to 1024x768
/// 
class HeadlessToolKit implements PortableToolKit {
    

    @Override
    public Dimension getScreenSize() {
        return new Dimension(1024, 768);
    }

    /// Same values as `Toolkit.getMenuShortcutKeyMask()`, which throws
    /// `HeadlessException` in headless mode: Command on macOS, Control elsewhere.
    @Deprecated
    @Override
    public int getMenuShortcutKeyMask() {
        return isMac() ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
    }

    private boolean isMac() {
        return PlatformDetection.getPlatform() == PlatformDetection.MACOSX;        
    }

    @Override
    public Clipboard getSystemClipboard() {
        return new Clipboard("headless clipboard");
    }
}

class SwingToolKit implements PortableToolKit {
    Toolkit toolkit;

    public SwingToolKit() {
        this.toolkit = Toolkit.getDefaultToolkit();
    }

    @Override 
    public Clipboard getSystemClipboard() {
        return toolkit.getSystemClipboard();
    }

    @Deprecated
    @Override 
    public int getMenuShortcutKeyMask() {
        return toolkit.getMenuShortcutKeyMask();
    }

    @Override 
    public Dimension getScreenSize() {
        return toolkit.getScreenSize();
    }
}
