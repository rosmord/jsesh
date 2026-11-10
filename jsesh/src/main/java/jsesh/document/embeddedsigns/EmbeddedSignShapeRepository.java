/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.observable.ObservableEventSupport;

import jsesh.glyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.HieroglyphShapeRepositoryChangedEvent;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.model.HieroglyphicText;
import jsesh.model.tools.HieroglyphCodesExtractor;
import jsesh.signcodes.CanonicalCode;

/// The shapes of the signs embedded in one document.
///
/// Put it in front of the global repository with a
/// [jsesh.glyphs.fonts.CompositeHieroglyphShapeRepository]. It can be
/// re-bound to another document's list with [#bind(EmbeddedSignList)], for
/// views which load another document. It fires a
/// [HieroglyphShapeRepositoryChangedEvent] whenever its signs change.
public final class EmbeddedSignShapeRepository implements HieroglyphShapeRepository {

    private EmbeddedSignList signs;
    private final Map<String, ShapeChar> cache = new HashMap<>();
    private boolean newSigns = false;
    private final ObservableEventSupport<HieroglyphShapeRepositoryChangedEvent> eventSupport = new ObservableEventSupport<>();
    private final ObservableEventListener<EmbeddedSignListChangedEvent> listListener = this::signChanged;

    /// A repository for the given list.
    public EmbeddedSignShapeRepository(EmbeddedSignList signs) {
        this.signs = signs;
        signs.addListener(listListener);
    }

    /// An empty repository, to be bound later.
    public EmbeddedSignShapeRepository() {
        this(new EmbeddedSignList());
    }

    /// Uses the signs of another list (for instance, when a view loads a new document).
    public void bind(EmbeddedSignList newSigns) {
        if (newSigns == signs) {
            return;
        }
        signs.removeListener(listListener);
        signs = newSigns;
        signs.addListener(listListener);
        cache.clear();
        this.newSigns = true;
        eventSupport.fireEvent(new HieroglyphShapeRepositoryChangedEvent());
    }

    /// The list currently used.
    public EmbeddedSignList getSigns() {
        return signs;
    }

    /// The embedded signs drawn by a repository, if it is (or contains, through
    /// [CompositeHieroglyphShapeRepository]s) an [EmbeddedSignShapeRepository].
    ///
    /// Lets code which only has a render context (exporters, for instance)
    /// find the signs of the document it draws.
    public static Optional<EmbeddedSignList> find(HieroglyphShapeRepository repository) {
        if (repository instanceof EmbeddedSignShapeRepository e) {
            return Optional.of(e.getSigns());
        }
        if (repository instanceof CompositeHieroglyphShapeRepository c) {
            for (HieroglyphShapeRepository r : c.getRepositories()) {
                Optional<EmbeddedSignList> found = find(r);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    /// The embedded signs of `repository` (see [#find]) which `text` uses.
    ///
    /// Used to write a fragment of text with the signs it needs, as in the MdC
    /// source stored in exported pictures.
    ///
    /// @return the signs used, or an empty list if there are none.
    public static EmbeddedSignList usedIn(HieroglyphicText text, HieroglyphShapeRepository repository) {
        return find(repository)
                .filter(list -> !list.isEmpty())
                .map(list -> list.restrictedTo(new HieroglyphCodesExtractor(false).extractHieroglyphs(text)))
                .filter(list -> !list.isEmpty())
                .orElseGet(EmbeddedSignList::new);
    }

    private void signChanged(EmbeddedSignListChangedEvent event) {
        cache.remove(event.code());
        newSigns = true;
        eventSupport.fireEvent(new HieroglyphShapeRepositoryChangedEvent());
    }

    @Override
    public ShapeChar get(CanonicalCode code) {
        String c = code.code();
        ShapeChar shape = cache.get(c);
        if (shape == null) {
            shape = signs.get(c).map(s -> EmbeddedSignShapes.toShape(s.code(), s.svg())).orElse(null);
            if (shape != null) {
                cache.put(c, shape);
            }
        }
        return shape;
    }

    @Override
    public ShapeChar getSmallBody(CanonicalCode code) {
        return null;
    }

    @Override
    public boolean hasNewSigns() {
        return newSigns;
    }

    @Override
    public Set<String> getCodes() {
        newSigns = false;
        Set<String> codes = new LinkedHashSet<>();
        for (EmbeddedSign s : signs.asList()) {
            codes.add(s.code());
        }
        return codes;
    }

    @Override
    public void addListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {
        eventSupport.addListener(listener);
    }

    @Override
    public void removeListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {
        eventSupport.removeListener(listener);
    }
}
