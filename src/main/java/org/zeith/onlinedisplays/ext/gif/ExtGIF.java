package org.zeith.onlinedisplays.ext.gif;

import net.minecraft.resources.ResourceLocation;
import org.zeith.onlinedisplays.client.texture.*;
import org.zeith.onlinedisplays.ext.gif.decoders.ADecoder;
import org.zeith.onlinedisplays.ext.gif.lib.GIFFrame;
import org.zeith.onlinedisplays.util.ExtensionParser;
import org.zeith.onlinedisplays.util.ImageData;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class ExtGIF
		extends ExtensionParser
{
	public ExtGIF()
	{
		super("gif");
	}
	
	static final String gif87a = "GIF87a";
	static final String gif89a = "GIF89a";
	
	@Override
	public boolean isMatchingBinary(ImageData raw)
	{
		byte[] fileBytes = raw.getData();
		
		return Arrays.equals(Arrays.copyOfRange(fileBytes, 0, 6), gif87a.getBytes()) ||
				Arrays.equals(Arrays.copyOfRange(fileBytes, 0, 6), gif89a.getBytes());
	}
	
	@Override
	public Optional<IDisplayableTexture> loadImage(ImageData data, ITextureFactory factory)
	{
		GIFFrame[] frames = ADecoder.decode(data);
		if(frames != null && frames.length > 0)
		{
			String hash = data.getHash();
			
			IDisplayableTexture[] framesTx = IntStream.range(0, frames.length)
					.mapToObj(i -> factory.createStaticImage(hash + "/" + (i + 1), frames[i].image))
					.toArray(IDisplayableTexture[]::new);
			
			int w = framesTx[0].getWidth();
			int h = framesTx[0].getHeight();
			
			int durationMS = Arrays.stream(frames).mapToInt(GIFFrame::getDelayMS).sum();
			ResourceLocation[] allTextures = new ResourceLocation[durationMS];
			
			int currentMS = 0;
			for(int i = 0; i < frames.length; ++i)
			{
				int fill = frames[i].getDelayMS();
				Arrays.fill(allTextures, currentMS, currentMS + fill, framesTx[i].getPath(0L));
				currentMS += fill;
			}
			
			return Optional.of(new AnimatedDisplayableTexture(allTextures, hash, w, h));
		}
		
		return Optional.empty();
	}
}