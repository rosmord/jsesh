package org.qenherkhopeshef.viewtoolkit.drawing.textOriented;

import java.util.Arrays;

/// The heights of a (possibly very long) list of items, with fast position
/// computations.
///
/// Each item has a height, which is either *exact* (the item has been laid
/// out) or an *estimate*. The index answers, in O(log n):
///
/// - the vertical position of an item (sum of the heights before it);
/// - the item found at a given vertical position.
///
/// Changing a height is O(log n). Inserting or removing items is O(n) (array
/// copies and a linear rebuild of the tree), which is still a matter of
/// milliseconds for a million items.
///
/// Implemented with a Fenwick (binary indexed) tree.
final class HeightIndex {

	private double[] heights = new double[16];

	private boolean[] exact = new boolean[16];

	/// Fenwick tree, 1-based: tree[i] covers the items (i - lowbit(i), i].
	private double[] tree = new double[17];

	private int size = 0;

	private int inexactCount = 0;

	int size() {
		return size;
	}

	double get(int i) {
		checkIndex(i);
		return heights[i];
	}

	boolean isExact(int i) {
		checkIndex(i);
		return exact[i];
	}

	/// Number of items whose height is an estimate.
	int getInexactCount() {
		return inexactCount;
	}

	void set(int i, double height, boolean isExact) {
		checkIndex(i);
		if (exact[i] != isExact)
			inexactCount += isExact ? -1 : 1;
		exact[i] = isExact;
		double delta = height - heights[i];
		heights[i] = height;
		if (delta != 0) {
			for (int k = i + 1; k <= size; k += k & -k)
				tree[k] += delta;
		}
	}

	/// Sum of the heights of items [0, i).
	double prefix(int i) {
		if (i < 0 || i > size)
			throw new IndexOutOfBoundsException(i);
		double sum = 0;
		for (int k = i; k > 0; k -= k & -k)
			sum += tree[k];
		return sum;
	}

	double total() {
		return prefix(size);
	}

	/// The index of the item which contains vertical position y, that is, the
	/// i such that prefix(i) <= y < prefix(i+1). Positions before the first
	/// item give 0, positions after the last one give size() - 1.
	///
	/// @return the item index, or -1 if the index is empty.
	int indexAt(double y) {
		if (size == 0)
			return -1;
		int pos = 0;
		double remaining = y;
		for (int step = Integer.highestOneBit(size); step > 0; step >>= 1) {
			int next = pos + step;
			if (next <= size && tree[next] <= remaining) {
				pos = next;
				remaining -= tree[next];
			}
		}
		return Math.min(pos, size - 1);
	}

	/// Inserts count items at index at, with an estimated height.
	void insert(int at, int count, double estimatedHeight) {
		if (at < 0 || at > size)
			throw new IndexOutOfBoundsException(at);
		ensureCapacity(size + count);
		System.arraycopy(heights, at, heights, at + count, size - at);
		System.arraycopy(exact, at, exact, at + count, size - at);
		Arrays.fill(heights, at, at + count, estimatedHeight);
		Arrays.fill(exact, at, at + count, false);
		size += count;
		inexactCount += count;
		rebuild();
	}

	/// Removes count items, starting at index at.
	void remove(int at, int count) {
		if (at < 0 || at + count > size)
			throw new IndexOutOfBoundsException(at);
		for (int i = at; i < at + count; i++) {
			if (!exact[i])
				inexactCount--;
		}
		System.arraycopy(heights, at + count, heights, at, size - at - count);
		System.arraycopy(exact, at + count, exact, at, size - at - count);
		size -= count;
		rebuild();
	}

	/// Replaces the content with n items with an estimated height.
	void reset(int n, double estimatedHeight) {
		size = 0;
		inexactCount = 0;
		insert(0, n, estimatedHeight);
	}

	private void ensureCapacity(int capacity) {
		if (heights.length < capacity) {
			int newCapacity = Math.max(capacity, heights.length * 2);
			heights = Arrays.copyOf(heights, newCapacity);
			exact = Arrays.copyOf(exact, newCapacity);
		}
	}

	/// Linear-time construction of the Fenwick tree.
	private void rebuild() {
		if (tree.length < size + 1)
			tree = new double[heights.length + 1];
		Arrays.fill(tree, 0);
		for (int i = 1; i <= size; i++) {
			tree[i] += heights[i - 1];
			int parent = i + (i & -i);
			if (parent <= size)
				tree[parent] += tree[i];
		}
	}

	private void checkIndex(int i) {
		if (i < 0 || i >= size)
			throw new IndexOutOfBoundsException(i);
	}
}
