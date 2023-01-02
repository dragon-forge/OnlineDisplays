package org.zeith.onlinedisplays.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileContainer implements IFileContainer
{
	private final File file;
	
	public FileContainer(File file)
	{
		this.file = file;
	}
	
	@Override
	public boolean exists()
	{
		return file.isFile();
	}
	
	@Override
	public String getName()
	{
		return file.getName();
	}
	
	@Override
	public InputStream openInput() throws IOException
	{
		return new FileInputStream(file);
	}
	
	@Override
	public long length()
	{
		return file.length();
	}
}