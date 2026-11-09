package com.zovo.villagerreplant.mixin;

import com.zovo.villagerreplant.Replanter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.FarmlandBlock;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(HarvestFarmland.class)
public abstract class HarvestFarmlandMixin {
	@Shadow private @Nullable BlockPos aboveFarmlandPos;
	@Shadow private long nextOkStartTime;

	// Before vanilla looks at the plot: if it is empty farmland, plant the remembered or matching crop first.
	// Vanilla then sees a young crop and moves on to the next plot, the same as after its own planting.
	@Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)V", at = @At("HEAD"))
	private void villagerreplant$plantFirst(ServerLevel level, Villager body, long timestamp, CallbackInfo ci) {
		BlockPos pos = this.aboveFarmlandPos;
		if (pos == null || timestamp <= this.nextOkStartTime || !pos.closerToCenterThan(body.position(), 1.0)) return;
		if (!level.getBlockState(pos).isAir() || !(level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock)) return;
		Replanter.beforeVanillaPlant(level, body, pos);
	}

	@Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
	private void villagerreplant$beforeHarvest(ServerLevel level, Villager body, long timestamp, CallbackInfo ci) {
		if (this.aboveFarmlandPos != null) Replanter.beforeHarvest(level, this.aboveFarmlandPos);
	}

	@Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER))
	private void villagerreplant$afterHarvest(ServerLevel level, Villager body, long timestamp, CallbackInfo ci) {
		if (this.aboveFarmlandPos != null) Replanter.afterHarvest(level, body, this.aboveFarmlandPos);
	}

	// Vanilla plants the first seed it carries. On a plot that is kept for a crop the farmer has no seed for, say "no seeds".
	@WrapOperation(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/Villager;hasFarmSeeds()Z"))
	private boolean villagerreplant$keepPlot(Villager body, Operation<Boolean> original, ServerLevel level, Villager self, long timestamp) {
		boolean has = original.call(body);
		if (has && this.aboveFarmlandPos != null && Replanter.reservedForMissingSeed(level, body, this.aboveFarmlandPos)) return false;
		return has;
	}
}
