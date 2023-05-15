package org.zeith.onlinedisplays.mixins;

import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(DimensionDataStorage.class)
public interface DimensionDataStorageAccessor
{
	@Invoker
	File callGetDataFile(String pth);
}
