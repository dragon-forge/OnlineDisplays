package org.zeith.onlinedisplays.client.texture;

import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class BufferedTexture
		extends SimpleTexture
		implements IDisplayableTexture
{
	public Supplier<byte[]> data;
	
	protected String hash;
	protected int width, height;
	
	public BufferedTexture(ResourceLocation path, String hash, Supplier<byte[]> data)
	{
		super(path);
		this.data = data;
	}
	
	@Override
	public String getHash()
	{
		return hash;
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
	
	@Override
	public ResourceLocation getPath(long timeMS)
	{
		return location;
	}
	
	@Override
	protected TextureData getTextureImage(IResourceManager resources)
	{
		try(ByteArrayInputStream in = new ByteArrayInputStream(data.get()))
		{
			NativeImage img = NativeImage.read(in);
			width = img.getWidth();
			height = img.getHeight();
			
			return new TextureData(null, img);
		} catch(IOException e)
		{
			return new SimpleTexture.TextureData(e);
		}
	}
	
	@Override
	public void reset(TextureManager p_215244_1_, IResourceManager p_215244_2_, ResourceLocation p_215244_3_, Executor p_215244_4_)
	{
		super.reset(p_215244_1_, p_215244_2_, p_215244_3_, p_215244_4_);
	}
}