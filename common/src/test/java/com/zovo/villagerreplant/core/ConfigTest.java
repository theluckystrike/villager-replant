package com.zovo.villagerreplant.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigTest {
	@TempDir Path dir;

	@Test void missingFileWritesDefaults() throws Exception {
		Path f = dir.resolve("config/villager-replant.json");
		Config c = Config.load(f);
		assertTrue(Files.exists(f));
		assertTrue(c.replantRightAway);
		assertTrue(c.keepPlotForSameCrop);
		assertEquals(32, c.seedsKeptFromComposter);
		assertEquals(30, c.memoryMinutes);
		assertTrue(Files.readString(f).contains("\"matchNeighbours\": true"));
		assertFalse(Files.readString(f).contains("warnings"));
	}

	@Test void readsValues() throws Exception {
		Path f = dir.resolve("c.json");
		Files.writeString(f, "{\"replantRightAway\":false,\"keepPlotForSameCrop\":false,\"memoryMinutes\":5,\"logReplants\":true}");
		Config c = Config.load(f);
		assertFalse(c.replantRightAway);
		assertFalse(c.keepPlotForSameCrop);
		assertEquals(5, c.memoryMinutes);
		assertEquals(5 * 60 * 20L, c.memoryTicks());
		assertTrue(c.logReplants);
		assertTrue(c.warnings.isEmpty());
	}

	@Test void badValuesFallBackWithWarnings() throws Exception {
		Path f = dir.resolve("c.json");
		Files.writeString(f, "{\"replantRightAway\":\"yes\",\"seedsKeptFromComposter\":99,\"memoryMinutes\":0,\"memoryPlotsPerWorld\":\"many\"}");
		Config c = Config.load(f);
		assertTrue(c.replantRightAway);
		assertEquals(30, c.memoryMinutes);
		assertEquals(20000, c.memoryPlotsPerWorld);
		assertEquals(32, c.seedsKeptFromComposter);
		assertEquals(4, c.warnings.size());
	}

	@Test void brokenJsonUsesDefaults() throws Exception {
		Path f = dir.resolve("c.json");
		Files.writeString(f, "{not json");
		Config c = Config.load(f);
		assertTrue(c.matchNeighbours);
		assertEquals(1, c.warnings.size());
	}
}
