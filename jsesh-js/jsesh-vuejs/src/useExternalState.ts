/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { onScopeDispose, shallowRef, type ShallowRef } from "vue";

/// Anything with a `subscribe` / `state` pair: `EditorSession` and `PaletteController` (`@jsesh/editor`,
/// `@jsesh/palette`) are such sources.
export interface ExternalStateSource<S> {
  readonly state: S;
  subscribe(listener: () => void): () => void;
}

/// Follows a source of state outside Vue as a `ref`. The source must return the same `state` object until its next
/// change (the JSesh ones do), so the ref changes exactly when the source does. The subscription ends with the
/// current scope (a component's setup, usually).
///
/// ```ts
/// const session = new EditorSession();
/// const state = useExternalState(session); // state.value.mdc, state.value.canUndo...
/// ```
export function useExternalState<S>(source: ExternalStateSource<S>): ShallowRef<S> {
  const state = shallowRef(source.state);
  onScopeDispose(source.subscribe(() => (state.value = source.state)));
  return state;
}
