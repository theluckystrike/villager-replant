package com.zovo.villagerreplant;

import com.zovo.villagerreplant.core.Config;
import com.zovo.villagerreplant.core.PlotMemory;
import com.zovo.villagerreplant.core.SeedPicker;
import com.zovo.villagerreplant.mixin.CropBlockInvoker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/** Server-thread glue between the farmer's harvest behavior and the pure seed choice logic. */
public final class Replanter {
	private static final Map<ServerLevel, PlotMemory> MEMORY = new WeakHashMap<>();
	private static long lastPrune;

	private Replanter() {}

	static PlotMemory memory(ServerLevel level) {
		Config c = VillagerReplantMod.CONFIG;
		return MEMORY.computeIfAbsent(level, l -> new PlotMemory(c.memoryPlotsPerWorld, c.memoryTicks()));
	}

	/** Seed id of a crop block, or null if the block is not a crop. */
	static String seedOf(BlockState state) {
		if (state.getBlock() instanceof CropBlock crop) {
			Item seed = ((CropBlockInvoker) crop).villagerreplant$seed().asItem();
			return BuiltInRegistries.ITEM.getKey(seed).toString();
		}
		return null;
	}

	/** Called just before the farmer breaks a ripe crop. */
	public static void beforeHarvest(ServerLevel level, BlockPos pos) {
		String seed = seedOf(level.getBlockState(pos));
		if (seed == null) return;
		long now = level.getGameTime();
		PlotMemory m = memory(level);
		m.remember(pos.asLong(), seed, now);
		if (now - lastPrune > 1200) {
			lastPrune = now;
			m.prune(now);
		}
	}

	/** Called just after the farmer broke a ripe crop. Replants the same crop if the farmer carries its seed. */
	public static void afterHarvest(ServerLevel level, Villager villager, BlockPos pos) {
		if (!VillagerReplantMod.CONFIG.replantRightAway) return;
		String seed = memory(level).get(pos.asLong(), level.getGameTime());
		if (seed == null) return;
		plantPreferred(level, villager, pos, seed, List.of(), "harvest");
	}

	/** Called when the farmer stands at an empty plot, before vanilla picks a seed. True if this mod planted. */
	public static boolean beforeVanillaPlant(ServerLevel level, Villager villager, BlockPos pos) {
		String remembered = memory(level).get(pos.asLong(), level.getGameTime());
		// A plot kept for its own crop never takes a neighbour's crop instead: only the remembered seed counts there.
		boolean kept = remembered != null && VillagerReplantMod.CONFIG.keepPlotForSameCrop;
		List<String> neighbours = VillagerReplantMod.CONFIG.matchNeighbours && !kept ? neighbourSeeds(level, pos) : List.of();
		if (remembered == null && neighbours.isEmpty()) return false;
		return plantPreferred(level, villager, pos, remembered, neighbours, remembered != null ? "memory" : "neighbours");
	}

	/**
	 * True if vanilla must not plant on this empty plot now: a crop was harvested here a short while ago and the
	 * farmer does not carry its seed. The plot stays empty until the farmer picks the seed up or the memory runs out.
	 */
	public static boolean reservedForMissingSeed(ServerLevel level, Villager villager, BlockPos pos) {
		if (!VillagerReplantMod.CONFIG.keepPlotForSameCrop) return false;
		String remembered = memory(level).get(pos.asLong(), level.getGameTime());
		if (remembered == null) return false;
		SimpleContainer inv = villager.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack st = inv.getItem(i);
			if (!st.isEmpty() && remembered.equals(BuiltInRegistries.ITEM.getKey(st.getItem()).toString())) return false;
		}
		if (VillagerReplantMod.CONFIG.logReplants) {
			VillagerReplantMod.LOG.info("Kept {} {} {} empty for {} (farmer has none)", pos.getX(), pos.getY(), pos.getZ(), remembered);
		}
		return true;
	}

	static List<String> neighbourSeeds(ServerLevel level, BlockPos pos) {
		List<String> out = new ArrayList<>(4);
		PlotMemory m = memory(level);
		long now = level.getGameTime();
		for (Direction d : Direction.Plane.HORIZONTAL) {
			BlockPos n = pos.relative(d);
			String s = seedOf(level.getBlockState(n));
			if (s == null && level.getBlockState(n).isAir()) s = m.get(n.asLong(), now);
			if (s != null) out.add(s);
		}
		return out;
	}

	private static boolean plantPreferred(ServerLevel level, Villager villager, BlockPos pos, String remembered, List<String> neighbours, String why) {
		if (!level.getBlockState(pos).isAir() || !(level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock)) return false;
		SimpleContainer inv = villager.getInventory();
		List<String> ids = new ArrayList<>(inv.getContainerSize());
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack st = inv.getItem(i);
			ids.add(st.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(st.getItem()).toString());
		}
		int slot = SeedPicker.pick(ids, remembered, neighbours, id -> {
			int i = ids.indexOf(id);
			return i >= 0 && canPlant(level, pos, inv.getItem(i));
		});
		if (slot < 0) return false;
		ItemStack stack = inv.getItem(slot);
		BlockState place = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
		level.setBlockAndUpdate(pos, place);
		level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(villager, place));
		level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
		stack.shrink(1);
		if (stack.isEmpty()) inv.setItem(slot, ItemStack.EMPTY);
		memory(level).forget(pos.asLong());
		if (VillagerReplantMod.CONFIG.logReplants) {
			VillagerReplantMod.LOG.info("Replanted {} at {} {} {} ({})", ids.get(slot), pos.getX(), pos.getY(), pos.getZ(), why);
		}
		return true;
	}

	static boolean canPlant(ServerLevel level, BlockPos pos, ItemStack stack) {
		return !stack.isEmpty() && stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) && stack.getItem() instanceof BlockItem bi
			&& bi.getBlock().defaultBlockState().canSurvive(level, pos);
	}
}
