package org.zeith.onlinedisplays.ext.webp;

import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.util.ExtensionParser;
import org.zeith.onlinedisplays.util.ImageData;
import org.zeith.webp4j.WebP;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

public class ExtWebP
		extends ExtensionParser
{
	public static final WebP WEBP = new WebP(OnlineDisplays.getModDir());
	
	public ExtWebP()
	{
		super("webp");
	}
	
	@Override
	public boolean isMatchingBinary(ImageData raw)
	{
		ByteBuffer buffer = ByteBuffer.wrap(raw.getData(), 0, 12);
		int riff = buffer.getInt();
		int chunkSize = buffer.getInt();
		int webp = buffer.getInt();
		return riff == 0x52494646 && webp == 0x57454250;
	}
	
	@Override
	public ImageData convert(ImageData raw)
	{
		File webpFile = null;
		try
		{
			String nfn = raw.getFileName();
			if(nfn.toLowerCase(Locale.ROOT).endsWith(".webp"))
				nfn = nfn.substring(0, nfn.length() - 5) + ".png";
			
			OnlineDisplays.LOG.info("Attempting to convert WebP " + raw.getHash() + " " + raw.getFileName() + "...");
			
			webpFile = File.createTempFile("onlinedisplays_webp", "tmpnet.webp");
			Files.write(webpFile.toPath(), raw.getData());
			Optional<byte[]> png = WEBP.convert(webpFile);
			webpFile.delete();
			
			OnlineDisplays.LOG.info("WebP " + raw.getHash() + " " + raw.getFileName() + " converted into " + png.orElse(new byte[0]).length + " bytes of PNG image.");
			return new ImageData(nfn, png.orElseThrow(NoSuchElementException::new));
		} catch(Exception e)
		{
			OnlineDisplays.LOG.error("Failed to convert WebP image to PNG with error:", e);
		}
		if(webpFile != null) webpFile.delete();
		return super.convert(raw);
	}
}