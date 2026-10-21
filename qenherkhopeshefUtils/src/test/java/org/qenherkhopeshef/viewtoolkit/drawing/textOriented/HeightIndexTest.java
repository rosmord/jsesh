package org.qenherkhopeshef.viewtoolkit.drawing.textOriented;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.viewtoolkit.drawing.textOriented.HeightIndex;

public class HeightIndexTest {

    @Test
    public void prefixAndTotal() {
        HeightIndex index = new HeightIndex();
        index.reset(5, 10);
        assertEquals(0, index.prefix(0));
        assertEquals(30, index.prefix(3));
        assertEquals(50, index.total());
        assertEquals(5, index.getInexactCount());
    }

    @Test
    public void setUpdatesPrefixesAndExactness() {
        HeightIndex index = new HeightIndex();
        index.reset(4, 10);
        index.set(1, 25, true);
        assertEquals(10, index.prefix(1));
        assertEquals(35, index.prefix(2));
        assertEquals(55, index.total());
        assertEquals(3, index.getInexactCount());
        assertTrue(index.isExact(1));
    }

    @Test
    public void indexAt() {
        HeightIndex index = new HeightIndex();
        index.reset(3, 10); // [0,10) [10,20) [20,30)
        assertEquals(0, index.indexAt(-5));
        assertEquals(0, index.indexAt(0));
        assertEquals(0, index.indexAt(9.9));
        assertEquals(1, index.indexAt(10));
        assertEquals(2, index.indexAt(29));
        assertEquals(2, index.indexAt(1000));
        assertEquals(-1, new HeightIndex().indexAt(3));
    }

    @Test
    public void insertAndRemove() {
        HeightIndex index = new HeightIndex();
        index.reset(3, 10);
        index.set(0, 1, true);
        index.set(2, 3, true);
        index.insert(1, 2, 100); // 1, 100, 100, 10, 3
        assertEquals(5, index.size());
        assertEquals(214, index.total());
        assertEquals(3, index.getInexactCount());
        assertEquals(10, index.get(3));
        index.remove(0, 2); // 100, 10, 3
        assertEquals(113, index.total());
        assertEquals(2, index.getInexactCount());
        assertTrue(index.isExact(2));
    }

    @Test
    public void randomOperationsMatchNaiveSums() {
        Random random = new Random(42);
        HeightIndex index = new HeightIndex();
        java.util.ArrayList<Double> naive = new java.util.ArrayList<>();
        for (int step = 0; step < 2000; step++) {
            int op = random.nextInt(3);
            if (op == 0 || naive.isEmpty()) {
                int at = random.nextInt(naive.size() + 1);
                int count = 1 + random.nextInt(5);
                index.insert(at, count, 7);
                for (int k = 0; k < count; k++)
                    naive.add(at, 7.0);
            } else if (op == 1) {
                int i = random.nextInt(naive.size());
                double h = random.nextInt(50);
                index.set(i, h, true);
                naive.set(i, h);
            } else {
                int i = random.nextInt(naive.size());
                index.remove(i, 1);
                naive.remove(i);
            }
            double sum = 0;
            for (int i = 0; i < naive.size(); i++) {
                assertEquals(sum, index.prefix(i), 1e-6);
                sum += naive.get(i);
            }
            assertEquals(sum, index.total(), 1e-6);
        }
    }
}
