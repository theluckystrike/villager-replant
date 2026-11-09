package com.zovo.villagerreplant.core;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remembers which crop grew on a plot, keyed by packed block position, for a limited time.
 * Bounded: when full, the oldest entry is dropped. Not thread safe (server thread only).
 */
public final class PlotMemory {
	private record Entry(String seed, long expiresAt) {}

	private final LinkedHashMap<Long, Entry> map = new LinkedHashMap<>();
	private final int capacity;
	private final long lifetimeTicks;

	public PlotMemory(int capacity, long lifetimeTicks) {
		this.capacity = Math.max(1, capacity);
		this.lifetimeTicks = Math.max(1, lifetimeTicks);
	}

	public void remember(long pos, String seed, long now) {
		map.remove(pos);
		map.put(pos, new Entry(seed, now + lifetimeTicks));
		while (map.size() > capacity) {
			Iterator<Long> it = map.keySet().iterator();
			it.next();
			it.remove();
		}
	}

	/** The remembered seed for this plot, or null if none or expired. */
	public String get(long pos, long now) {
		Entry e = map.get(pos);
		if (e == null) return null;
		if (now >= e.expiresAt) {
			map.remove(pos);
			return null;
		}
		return e.seed;
	}

	public void forget(long pos) {
		map.remove(pos);
	}

	/** Drops expired entries. Call now and then; get() also drops them lazily. */
	public void prune(long now) {
		map.values().removeIf(e -> now >= e.expiresAt);
	}

	public int size() {
		return map.size();
	}

	Map<Long, Entry> view() {
		return map;
	}
}
