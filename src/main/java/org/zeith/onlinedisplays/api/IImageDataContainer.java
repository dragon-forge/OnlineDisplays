package org.zeith.onlinedisplays.api;

import org.zeith.onlinedisplays.util.ImageData;

public interface IImageDataContainer
{
	IImageDataContainer DUMMY = new IImageDataContainer()
	{
		@Override
		public ImageData load(String hash)
		{
			return null;
		}
		
		@Override
		public boolean has(String hash)
		{
			return false;
		}
		
		@Override
		public void save(ImageData data)
		{
		}
	};
	
	ImageData load(String hash);
	
	boolean has(String hash);
	
	void save(ImageData data);
}