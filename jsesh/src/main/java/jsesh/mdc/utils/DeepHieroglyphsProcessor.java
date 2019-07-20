package jsesh.mdc.utils;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.TopItem;

import java.util.List;
import java.util.function.Consumer;

/**
 * A command which works on hieroglyphs.
 */

public class DeepHieroglyphsProcessor {

	/**
	 * Apply Consumer hieroglyphicProcessors to all glyphs in this document.
	 */
	public final void processItems(List<TopItem> items, Consumer<Hieroglyph> hieroglyphProcessor) {
		HieroglyphicFilter filter= new HieroglyphicFilter(hieroglyphProcessor);
		for (TopItem item: items) {
			item.accept(filter);
		}
	}

	private class HieroglyphicFilter extends ModelElementDeepAdapter {
		private Consumer<Hieroglyph> hieroglyphConsumer;

		public HieroglyphicFilter(Consumer<Hieroglyph> hieroglyphConsumer) {
			this.hieroglyphConsumer = hieroglyphConsumer;
		}

		@Override
		public void visitHieroglyph(Hieroglyph h) {
			hieroglyphConsumer.accept(h);
		}
	}
}
