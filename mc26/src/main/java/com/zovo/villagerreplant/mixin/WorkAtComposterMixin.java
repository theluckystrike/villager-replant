package com.zovo.villagerreplant.mixin;

import com.zovo.villagerreplant.VillagerReplantMod;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla farmers put wheat and beetroot seeds in the composter until they have 10 of each left, and then run out of
 * seeds for their own fields. This raises the number they keep (config seedsKeptFromComposter, default 32).
 */
@Mixin(WorkAtComposter.class)
public abstract class WorkAtComposterMixin {
	@ModifyConstant(method = "compostItems", constant = @Constant(intValue = 10), require = 1)
	private int villagerreplant$seedsToKeep(int vanilla) {
		return VillagerReplantMod.CONFIG.seedsKeptFromComposter;
	}
}
