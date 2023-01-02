package org.zeith.onlinedisplays.util.io;

import java.io.*;

public class VirtualFileContainer
		implements IFileContainer
{
	private final String name;
	private final byte[] data;
	
	public VirtualFileContainer(String name, byte[] data)
	{
		this.name = name;
		this.data = data;
	}
	
	@Override
	public boolean exists()
	{
		return true;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public InputStream openInput() throws IOException
	{
		return new ByteArrayInputStream(data);
	}
	
	@Override
	public long length()
	{
		return data.length;
	}
}