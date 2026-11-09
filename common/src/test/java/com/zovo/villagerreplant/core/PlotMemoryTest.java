package com.zovo.villagerreplant.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PlotMemoryTest {
	@Test void remembersUntilExpiry() {
		PlotMemory m = new PlotMemory(10, 100);
		m.remember(5L, "minecraft:carrot", 1000);
		assertEquals("minecraft:carrot", m.get(5L, 1099));
		assertNull(m.get(5L, 1100));
		assertEquals(0, m.size());
	}

	@Test void newerHarvestReplacesOlder() {
		PlotMemory m = new PlotMemory(10, 100);
		m.remember(5L, "minecraft:carrot", 0);
		m.remember(5L, "minecraft:potato", 10);
		assertEquals("minecraft:potato", m.get(5L, 50));
		assertEquals(1, m.size());
	}

	@Test void capacityDropsOldest() {
		PlotMemory m = new PlotMemory(3, 1000);
		for (long i = 0; i < 5; i++) m.remember(i, "s" + i, i);
		assertEquals(3, m.size());
		assertNull(m.get(0L, 10));
		assertNull(m.get(1L, 10));
		assertEquals("s4", m.get(4L, 10));
	}

	@Test void rememberingAgainMovesToNewest() {
		PlotMemory m = new PlotMemory(2, 1000);
		m.remember(1L, "a", 0);
		m.remember(2L, "b", 0);
		m.remember(1L, "a", 1);
		m.remember(3L, "c", 2);
		assertEquals("a", m.get(1L, 5));
		assertNull(m.get(2L, 5));
	}

	@Test void pruneDropsExpired() {
		PlotMemory m = new PlotMemory(10, 10);
		m.remember(1L, "a", 0);
		m.remember(2L, "b", 5);
		m.prune(12);
		assertEquals(1, m.size());
		m.forget(2L);
		assertEquals(0, m.size());
	}
}
