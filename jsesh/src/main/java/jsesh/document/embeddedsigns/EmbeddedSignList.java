/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;

import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.observable.ObservableEventSupport;

/// The signs embedded in one document ("text-specific signs").
///
/// Each sign gets the code `sign<n>`, where `n` is one more than the
/// highest number ever used in the document ([#getMaxId()]). Removing a sign
/// does **not** free its number: the next sign gets a new one, so an old copy
/// of the text never silently shows another drawing.
///
/// The list is observable; changes are not undoable.
public final class EmbeddedSignList {

    /// The prefix of all embedded sign codes.
    public static final String CODE_PREFIX = "sign";

    private final TreeMap<Integer, EmbeddedSign> signs = new TreeMap<>();
    private int maxId = 0;
    private final ObservableEventSupport<EmbeddedSignListChangedEvent> eventSupport = new ObservableEventSupport<>();

    /// Is `code` of the form `sign<n>`, with `n > 0`.
    public static boolean isEmbeddedSignCode(String code) {
        return idOf(code).isPresent();
    }

    /// The number of an embedded sign code: 12 for `sign12`.
    public static OptionalInt idOf(String code) {
        if (code == null || !code.startsWith(CODE_PREFIX) || code.length() == CODE_PREFIX.length()) {
            return OptionalInt.empty();
        }
        String digits = code.substring(CODE_PREFIX.length());
        if (!digits.chars().allMatch(c -> c >= '0' && c <= '9') || digits.startsWith("0")
                || digits.length() > 9) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(digits));
    }

    /// Adds a new sign, with a fresh code.
    ///
    /// @param svg         the SVG data, as the user's file has it (see
    ///                    [EmbeddedSignShapes#checkSvg]).
    /// @param variantOf   the Gardiner code the sign is a variant of, or `null`.
    /// @param description a description, or `null`.
    /// @return the new sign.
    public EmbeddedSign add(byte[] svg, String variantOf, String description) {
        int id = maxId + 1;
        EmbeddedSign sign = new EmbeddedSign(CODE_PREFIX + id, svg, variantOf, description);
        signs.put(id, sign);
        maxId = id;
        eventSupport.fireEvent(new EmbeddedSignListChangedEvent(sign.code()));
        return sign;
    }

    /// Puts a sign with its own code, replacing any sign with the same code.
    ///
    /// Used when reading a document. [#getMaxId()] grows if needed.
    public void put(EmbeddedSign sign) {
        int id = idOf(sign.code()).getAsInt();
        signs.put(id, sign);
        maxId = Math.max(maxId, id);
        eventSupport.fireEvent(new EmbeddedSignListChangedEvent(sign.code()));
    }

    /// Raises the highest number used so far. A smaller value is ignored.
    public void raiseMaxId(int id) {
        maxId = Math.max(maxId, id);
    }

    /// The highest number ever used for a sign of this document.
    public int getMaxId() {
        return maxId;
    }

    /// The sign with this code, if any.
    public Optional<EmbeddedSign> get(String code) {
        OptionalInt id = idOf(code);
        if (id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(signs.get(id.getAsInt()));
    }

    /// Is there a sign with this code.
    public boolean contains(String code) {
        return get(code).isPresent();
    }

    /// Removes a sign. Its number is not reused.
    ///
    /// @return true if a sign was removed.
    public boolean remove(String code) {
        OptionalInt id = idOf(code);
        if (id.isEmpty() || signs.remove(id.getAsInt()) == null) {
            return false;
        }
        eventSupport.fireEvent(new EmbeddedSignListChangedEvent(code));
        return true;
    }

    /// Changes the "variant of" code and the description of a sign.
    ///
    /// @return the updated sign.
    /// @throws IllegalArgumentException if there is no such sign.
    public EmbeddedSign update(String code, String variantOf, String description) {
        EmbeddedSign old = get(code).orElseThrow(() -> new IllegalArgumentException("No embedded sign " + code));
        EmbeddedSign updated = new EmbeddedSign(code, old.svg(), variantOf, description);
        signs.put(idOf(code).getAsInt(), updated);
        eventSupport.fireEvent(new EmbeddedSignListChangedEvent(code));
        return updated;
    }

    /// A new list holding the signs of this one whose codes are in `codes`,
    /// with the same [#getMaxId()].
    ///
    /// Used to store with a fragment of text only the signs it uses.
    public EmbeddedSignList restrictedTo(Collection<String> codes) {
        EmbeddedSignList result = new EmbeddedSignList();
        for (EmbeddedSign s : signs.values()) {
            if (codes.contains(s.code())) {
                result.put(s);
            }
        }
        result.raiseMaxId(maxId);
        return result;
    }

    /// The signs, by increasing number.
    public List<EmbeddedSign> asList() {
        return Collections.unmodifiableList(new ArrayList<>(signs.values()));
    }

    public boolean isEmpty() {
        return signs.isEmpty();
    }

    public int size() {
        return signs.size();
    }

    public void addListener(ObservableEventListener<? super EmbeddedSignListChangedEvent> listener) {
        eventSupport.addListener(listener);
    }

    public void removeListener(ObservableEventListener<? super EmbeddedSignListChangedEvent> listener) {
        eventSupport.removeListener(listener);
    }
}
