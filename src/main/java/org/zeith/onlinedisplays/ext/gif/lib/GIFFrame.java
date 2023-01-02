package org.zeith.onlinedisplays.ext.gif.lib;

import java.awt.image.BufferedImage;

public class GIFFrame
{
	public final BufferedImage image;
	public final int delayMS;
	
	public GIFFrame(BufferedImage img, int delay)
	{
		this.image = img;
		this.delayMS = delay * 10;
	}
	
	public int getDelayMS()
	{
		return delayMS;
	}
}