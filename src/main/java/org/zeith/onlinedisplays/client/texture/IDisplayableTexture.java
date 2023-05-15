package org.zeith.onlinedisplays.client.texture;

import net.minecraft.resources.ResourceLocation;

public interface IDisplayableTexture
{
	String getHash();
	
	ResourceLocation getPath(long timeMS);
	
	int getWidth();
	
	int getHeight();
}