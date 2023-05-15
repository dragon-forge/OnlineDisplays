package org.zeith.onlinedisplays.client.texture;

import net.minecraft.resources.ResourceLocation;

public class AnimatedDisplayableTexture
		implements IDisplayableTexture
{
	public final ResourceLocation[] textureByMS;
	public final String hash;
	public final int width, height;
	
	public AnimatedDisplayableTexture(ResourceLocation[] textureByMS, String hash, int width, int height)
	{
		this.textureByMS = textureByMS;
		this.hash = hash;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String getHash()
	{
		return hash;
	}
	
	@Override
	public ResourceLocation getPath(long timeMS)
	{
		return textureByMS[(int) Math.abs(timeMS % textureByMS.length)];
	}
	
	@Override
	public int getWidth()
	{
		return width;
	}
	
	@Override
	public int getHeight()
	{
		return height;
	}
}