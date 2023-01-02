package org.zeith.onlinedisplays.util;

import com.google.common.base.Suppliers;
import org.zeith.onlinedisplays.client.texture.IDisplayableTexture;
import org.zeith.onlinedisplays.level.LevelImageStorage;

import java.io.*;
import java.util.function.Supplier;

public class ImageData
{
	protected final String fileName;
	protected final byte[] data;
	
	protected Supplier<String> hash = Suppliers.memoize(() -> LevelImageStorage.HASH.hashify(ImageData.this.data));
	
	public ImageData(String fileName, byte[] data)
	{
		this.fileName = fileName;
		this.data = data;
	}
	
	public ImageData(DataInputStream in) throws IOException
	{
		fileName = in.readUTF();
		
		data = new byte[in.readInt()];
		in.readFully(data);
	}
	
	public void write(DataOutputStream out) throws IOException
	{
		out.writeUTF(fileName);
		
		out.writeInt(data.length);
		out.write(data);
	}
	
	public String getHash()
	{
		return hash.get();
	}
	
	public byte[] getData()
	{
		return data;
	}
	
	public String getFileName()
	{
		return fileName;
	}
}