package com.zovo.villagerreplant.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** config/villager-replant.json. Missing or bad values fall back to defaults and add a warning. */
public final class Config {
	public boolean replantRightAway = true;
	public boolean matchNeighbours = true;
	public boolean keepPlotForSameCrop = true;
	public int memoryMinutes = 30;
	public int seedsKeptFromComposter = 32;
	public int memoryPlotsPerWorld = 20000;
	public boolean logReplants = false;
	public final transient List<String> warnings = new ArrayList<>();

	public static Config defaults() {
		return new Config();
	}

	public long memoryTicks() {
		return memoryMinutes * 60L * 20L;
	}

	public static Config load(Path file) {
		Config c = new Config();
		if (!Files.exists(file)) {
			c.save(file);
			return c;
		}
		JsonObject o;
		try {
			JsonElement e = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
			if (!e.isJsonObject()) throw new IllegalStateException("not a JSON object");
			o = e.getAsJsonObject();
		} catch (Exception ex) {
			c.warnings.add("could not read the file (" + ex.getMessage() + "), using defaults");
			return c;
		}
		c.replantRightAway = bool(o, "replantRightAway", c.replantRightAway, c);
		c.matchNeighbours = bool(o, "matchNeighbours", c.matchNeighbours, c);
		c.keepPlotForSameCrop = bool(o, "keepPlotForSameCrop", c.keepPlotForSameCrop, c);
		c.memoryMinutes = intIn(o, "memoryMinutes", c.memoryMinutes, 1, 24 * 60, c);
		c.seedsKeptFromComposter = intIn(o, "seedsKeptFromComposter", c.seedsKeptFromComposter, 0, 64, c);
		c.memoryPlotsPerWorld = intIn(o, "memoryPlotsPerWorld", c.memoryPlotsPerWorld, 100, 1_000_000, c);
		c.logReplants = bool(o, "logReplants", c.logReplants, c);
		return c;
	}

	public void save(Path file) {
		try {
			Files.createDirectories(file.getParent());
			Gson g = new GsonBuilder().setPrettyPrinting().create();
			Files.writeString(file, g.toJson(this) + "\n", StandardCharsets.UTF_8);
		} catch (IOException ex) {
			warnings.add("could not write " + file + ": " + ex.getMessage());
		}
	}

	private static boolean bool(JsonObject o, String k, boolean def, Config c) {
		if (!o.has(k)) return def;
		JsonElement e = o.get(k);
		if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean()) return e.getAsBoolean();
		c.warnings.add(k + " must be true or false, using " + def);
		return def;
	}

	private static int intIn(JsonObject o, String k, int def, int min, int max, Config c) {
		if (!o.has(k)) return def;
		JsonElement e = o.get(k);
		try {
			int v = e.getAsInt();
			if (v < min || v > max) {
				c.warnings.add(k + " must be from " + min + " to " + max + ", using " + def);
				return def;
			}
			return v;
		} catch (Exception ex) {
			c.warnings.add(k + " must be a whole number, using " + def);
			return def;
		}
	}
}
