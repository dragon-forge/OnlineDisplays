package org.zeith.onlinedisplays.util;

import org.zeith.onlinedisplays.client.texture.IDisplayableTexture;
import org.zeith.onlinedisplays.client.texture.ITextureFactory;
import org.zeith.onlinedisplays.ext.gif.decoders.ADecoder;
import org.zeith.onlinedisplays.ext.gif.lib.GIFFrame;

import java.util.Locale;
import java.util.Optional;

public class ExtensionParser
{
	public static final ExtensionParser IDENTITY = new ExtensionParser("png");
	
	public final String extension;
	
	public ExtensionParser(String extension)
	{
		this.extension = extension.toLowerCase(Locale.ROOT);
	}
	
	public boolean isMatchingBinary(ImageData raw)
	{
		return false;
	}
	
	public Optional<IDisplayableTexture> loadImage(ImageData data, ITextureFactory factory)
	{
		GIFFrame[] frames = ADecoder.decode(data);
		
		return Optional.empty();
	}
	
	public ImageData convert(ImageData raw)
	{
		return raw;
	}
}