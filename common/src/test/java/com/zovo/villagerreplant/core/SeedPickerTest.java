package com.zovo.villagerreplant.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class SeedPickerTest {
	static final Predicate<String> TAG = Set.of("minecraft:wheat_seeds", "minecraft:carrot", "minecraft:potato", "minecraft:beetroot_seeds")::contains;
	static final List<String> INV = Arrays.asList("minecraft:wheat_seeds", null, "minecraft:bread", "minecraft:carrot", "minecraft:potato");

	@Test void rememberedSeedWins() {
		assertEquals(3, SeedPicker.pick(INV, "minecraft:carrot", List.of("minecraft:potato", "minecraft:potato"), TAG));
	}

	@Test void rememberedSeedMissingFallsBackToNeighbours() {
		assertEquals(4, SeedPicker.pick(INV, "minecraft:beetroot_seeds", List.of("minecraft:potato", "minecraft:carrot", "minecraft:potato"), TAG));
	}

	@Test void noMemoryNoNeighboursLeavesItToVanilla() {
		assertEquals(-1, SeedPicker.pick(INV, null, List.of(), TAG));
	}

	@Test void neighbourTieTakesEarlierSlot() {
		assertEquals(3, SeedPicker.pick(INV, null, List.of("minecraft:potato", "minecraft:carrot"), TAG));
	}

	@Test void neighbourSeedNotInInventoryIsSkipped() {
		assertEquals(-1, SeedPicker.pick(INV, null, List.of("minecraft:beetroot_seeds", "minecraft:beetroot_seeds"), TAG));
	}

	@Test void itemThatIsNotPlantableIsNeverPicked() {
		assertEquals(-1, SeedPicker.pick(INV, "minecraft:bread", List.of("minecraft:bread"), TAG));
	}

	@Test void nullNeighboursAreIgnored() {
		assertEquals(0, SeedPicker.pick(INV, null, Arrays.asList(null, "minecraft:wheat_seeds"), TAG));
	}
}
