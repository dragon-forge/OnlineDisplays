package org.zeith.onlinedisplays.util;

import java.util.Locale;

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
	
	public ImageData toPNG(ImageData raw)
	{
		return raw;
	}
}