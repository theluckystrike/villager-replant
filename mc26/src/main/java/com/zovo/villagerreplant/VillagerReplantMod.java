package com.zovo.villagerreplant;

import com.zovo.villagerreplant.core.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VillagerReplantMod implements ModInitializer {
	public static final Logger LOG = LoggerFactory.getLogger("villager-replant");
	public static volatile Config CONFIG = Config.defaults();

	@Override
	public void onInitialize() {
		Config cfg = Config.load(FabricLoader.getInstance().getConfigDir().resolve("villager-replant.json"));
		for (String w : cfg.warnings) LOG.warn("villager-replant.json: {}", w);
		CONFIG = cfg;
		LOG.info("Villager Replant ready: replantRightAway={}, matchNeighbours={}, memoryMinutes={}",
			cfg.replantRightAway, cfg.matchNeighbours, cfg.memoryMinutes);
	}
}
