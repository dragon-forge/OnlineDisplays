package org.zeith.onlinedisplays.ext.gif.lib;

import java.awt.image.BufferedImage;

public class GIFTicker
{
	private final GIFFrame[] frames;
	private final long[] offsets;
	private long startTime;
	private long fullCycle;
	private int curFrame;
	
	public GIFTicker(GIFFrame... frames)
	{
		this.frames = frames;
		this.offsets = new long[frames.length];
		long co = 0L;
		for(int i = 0; i < frames.length; ++i)
		{
			GIFFrame f = frames[i];
			this.offsets[i] = co;
			co += (long) f.delayMS;
		}
		this.fullCycle = co;
		this.reset();
	}
	
	public void reset()
	{
		this.startTime = System.currentTimeMillis();
	}
	
	public void tick()
	{
		long c = System.currentTimeMillis();
		while(c - this.startTime >= this.fullCycle)
			this.startTime += (c - this.startTime) / this.fullCycle * this.fullCycle;
	}
	
	public int getCurrentFrame()
	{
		long c = System.currentTimeMillis();
		int cf = 0;
		while(c - this.startTime >= this.offsets[cf])
		{
			if(++cf < this.offsets.length)
				continue;
			this.reset();
			return 0;
		}
		return cf;
	}
	
	public void setCurrentFrame(int i)
	{
		this.reset();
		this.startTime -= this.offsets[i];
	}
	
	public BufferedImage getImage()
	{
		return this.frames[this.getCurrentFrame()].image;
	}
}
