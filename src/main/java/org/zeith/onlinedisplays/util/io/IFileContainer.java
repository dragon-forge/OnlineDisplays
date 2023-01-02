package org.zeith.onlinedisplays.util.io;

import java.io.IOException;
import java.io.InputStream;

public interface IFileContainer
{
	boolean exists();
	
	String getName();
	
	InputStream openInput() throws IOException;
	
	long length();
}