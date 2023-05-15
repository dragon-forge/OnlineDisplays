package org.zeith.onlinedisplays.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
		this.hash = hash;
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
	protected TextureImage getTextureImage(ResourceManager resources)
	{
		try(ByteArrayInputStream in = new ByteArrayInputStream(data.get()))
		{
			NativeImage img = NativeImage.read(in);
			width = img.getWidth();
			height = img.getHeight();
			
			return new TextureImage(null, img);
		} catch(IOException e)
		{
			return new TextureImage(e);
		}
	}
}