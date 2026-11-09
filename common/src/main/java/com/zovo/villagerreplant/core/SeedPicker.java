package com.zovo.villagerreplant.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Chooses which inventory slot a farmer plants from. Pure logic: items are given as registry ids. */
public final class SeedPicker {
	private SeedPicker() {}

	/**
	 * @param slots      item id per inventory slot, null for an empty slot
	 * @param remembered seed id of the crop harvested on this plot, or null
	 * @param neighbours seed ids of the crops on the plots next to this one (may repeat, may be empty)
	 * @param plantable  true if the villager may plant this item here
	 * @return the slot to plant from, or -1 to leave the choice to vanilla
	 */
	public static int pick(List<String> slots, String remembered, List<String> neighbours, Predicate<String> plantable) {
		if (remembered != null) {
			int s = slotOf(slots, remembered, plantable);
			if (s >= 0) return s;
		}
		if (neighbours.isEmpty()) return -1;
		Map<String, Integer> count = new HashMap<>();
		for (String n : neighbours) if (n != null) count.merge(n, 1, Integer::sum);
		String best = null;
		int bestCount = 0;
		int bestSlot = -1;
		for (Map.Entry<String, Integer> e : count.entrySet()) {
			int s = slotOf(slots, e.getKey(), plantable);
			if (s < 0) continue;
			// more neighbours wins; on a tie the seed in the earlier slot wins, so the result does not depend on map order
			if (e.getValue() > bestCount || (e.getValue() == bestCount && s < bestSlot)) {
				best = e.getKey();
				bestCount = e.getValue();
				bestSlot = s;
			}
		}
		return best == null ? -1 : bestSlot;
	}

	static int slotOf(List<String> slots, String id, Predicate<String> plantable) {
		if (!plantable.test(id)) return -1;
		for (int i = 0; i < slots.size(); i++) if (id.equals(slots.get(i))) return i;
		return -1;
	}
}
