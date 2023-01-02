package org.zeith.onlinedisplays.client.texture;

import net.minecraft.util.ResourceLocation;

public interface IDisplayableTexture
{
	String getHash();
	
	ResourceLocation getPath(long timeMS);
	
	int getWidth();
	
	int getHeight();
}