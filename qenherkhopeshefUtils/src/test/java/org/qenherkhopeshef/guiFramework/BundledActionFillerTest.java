package org.qenherkhopeshef.guiFramework;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Verifies that {@link BundledActionFiller} prefers the new, lowerCamelCase
 * suffixes ({@code .text}, {@code .toolTipText}, {@code .accelerator},
 * {@code .mnemonic}, {@code .icon}) over the legacy PascalCase
 * {@code javax.swing.Action}-constant suffixes ({@code .Name},
 * {@code .ShortDescription}, {@code .AcceleratorKey}, {@code .MnemonicKey},
 * {@code .SmallIcon}), while still falling back to the legacy suffix when
 * the new one isn't defined.
 */
public class BundledActionFillerTest {

    private static Action newAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            }
        };
    }

    @Test
    public void newSuffixOnlyPopulatesName() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.text", "Hello");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals("Hello", action.getValue(Action.NAME));
    }

    @Test
    public void legacySuffixOnlyPopulatesName() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.Name", "Hello");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals("Hello", action.getValue(Action.NAME));
    }

    @Test
    public void bothSuffixesPresentNewWins() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.Name", "Old");
        defaults.put("foo.text", "New");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals("New", action.getValue(Action.NAME));
    }

    @Test
    public void neitherSuffixPresentLeavesNameNull() {
        AppDefaults defaults = new AppDefaults();
        Action action = newAction();
        assertDoesNotThrow(() -> BundledActionFiller.initActionProperties(action, "foo", defaults));
        assertNull(action.getValue(Action.NAME));
    }

    @Test
    public void toolTipNewSuffixWins() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.ShortDescription", "Old tooltip");
        defaults.put("foo.toolTipText", "New tooltip");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals("New tooltip", action.getValue(Action.SHORT_DESCRIPTION));
    }

    @Test
    public void acceleratorNewSuffixIsParsedAsKeyStroke() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.accelerator", "control C");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals(KeyStroke.getKeyStroke("control C"), action.getValue(Action.ACCELERATOR_KEY));
    }

    /**
     * Mirrors the two real production entries that combine "shortcut"
     * substitution with a mac-specific override (edit.redo,
     * text.groupHorizontally): both the base and the "[mac]" sibling must be
     * migrated to the new suffix together, or the mac override silently
     * stops applying (see the plan's migration checklist).
     */
    private static KeyStroke expectedShortcutKeyStroke(String suffix) {
        int shortCutMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        String replaceString = "control";
        if (shortCutMask == KeyEvent.META_MASK) {
            replaceString = "meta";
        } else if (shortCutMask == KeyEvent.ALT_MASK) {
            replaceString = "alt";
        }
        return KeyStroke.getKeyStroke(replaceString + " " + suffix);
    }

    @Test
    public void acceleratorMacVariantAppliesOnMac() {
        assumeTrue(PlatformDetection.getPlatform() == PlatformDetection.MACOSX,
                "mac-specific accelerator override only applies when running on macOS");
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.accelerator", "shortcut Y");
        defaults.put("foo.accelerator[mac]", "shortcut shift Z");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals(expectedShortcutKeyStroke("shift Z"), action.getValue(Action.ACCELERATOR_KEY));
    }

    @Test
    public void acceleratorNonMacPlatformIgnoresMacVariant() {
        assumeTrue(PlatformDetection.getPlatform() != PlatformDetection.MACOSX,
                "the [mac] override is only skipped when NOT running on macOS");
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.accelerator", "shortcut Y");
        defaults.put("foo.accelerator[mac]", "shortcut shift Z");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals(expectedShortcutKeyStroke("Y"), action.getValue(Action.ACCELERATOR_KEY));
    }

    @Test
    public void mnemonicNewSuffixParsedAsKeyCode() {
        // Single uppercase letters happen to parse under both BundledActionFiller's
        // KeyStroke-string convention and JHotDraw's bare-character convention
        // (matching production's "text.mnemonic=G") - that's a coincidence of the
        // Java KeyEvent.VK_<letter> naming, not a guarantee for arbitrary values.
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.mnemonic", "A");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertEquals(KeyEvent.VK_A, action.getValue(Action.MNEMONIC_KEY));
    }

    @Test
    public void iconNewSuffixWins() {
        Icon oldIcon = trivialIcon();
        Icon newIcon = trivialIcon();
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.SmallIcon", oldIcon);
        defaults.put("foo.icon", newIcon);
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertSame(newIcon, action.getValue(Action.SMALL_ICON));
    }

    @Test
    public void extensionSuffixIsNeverAliased() {
        AppDefaults defaults = new AppDefaults();
        defaults.put("foo.Preconditions", "Cond1");
        // A would-be lowerCamelCase spelling: guiFramework-only extensions have no
        // JHotDraw equivalent and must never be aliased, so this must be ignored.
        defaults.put("foo.preconditions", "Cond2, Cond3");
        Action action = newAction();
        BundledActionFiller.initActionProperties(action, "foo", defaults);
        assertArrayEquals(new String[] { "Cond1" },
                (String[]) action.getValue(BundledActionFiller.PRECONDITIONS));
    }

    private static Icon trivialIcon() {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
            }

            public int getIconWidth() {
                return 1;
            }

            public int getIconHeight() {
                return 1;
            }
        };
    }
}
