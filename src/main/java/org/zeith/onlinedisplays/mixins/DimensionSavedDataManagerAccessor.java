package org.zeith.onlinedisplays.mixins;

import net.minecraft.world.storage.DimensionSavedDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(DimensionSavedDataManager.class)
public interface DimensionSavedDataManagerAccessor
{
	@Invoker
	File callGetDataFile(String p_215754_1_);
}
