package org.zeith.onlinedisplays.mixins;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureAtlas.class)
public interface AtlasTextureAccessor
{
	@Invoker
	ResourceLocation callGetResourceLocation(ResourceLocation p_195420_1_);
}