package org.zeith.onlinedisplays.mixins.client;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemOverrides.class)
public interface ItemOverridesAccessor
{
	@Accessor
	ItemOverrides.BakedOverride[] getOverrides();
}