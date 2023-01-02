package org.zeith.onlinedisplays.mixins;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AtlasTexture.class)
public interface AtlasTextureAccessor
{
	@Invoker
	ResourceLocation callGetResourceLocation(ResourceLocation p_195420_1_);
}
