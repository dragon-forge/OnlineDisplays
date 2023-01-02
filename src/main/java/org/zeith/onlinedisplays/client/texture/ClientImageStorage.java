package org.zeith.onlinedisplays.client.texture;

import net.minecraft.util.StringUtils;
import org.zeith.hammerlib.net.Network;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.net.PacketRequestImageData;
import org.zeith.onlinedisplays.util.ImageData;

import java.io.*;
import java.util.*;

public class ClientImageStorage
{
	static final Map<String, Boolean> HASHED = new HashMap<>();
	static final Set<String> REQUESTED = new HashSet<>();
	
	public static File getImageCacheDir()
	{
		File hid = new File(OnlineDisplays.getModHiddenDir(), "image_cache");
		if(!hid.isDirectory())
			hid.mkdirs();
		return hid;
	}
	
	public static void clearRequestFlag(String hash)
	{
		REQUESTED.remove(hash);
	}
	
	private static File getHashFile(String hash)
	{
		File fl = new File(getImageCacheDir(), hash.substring(0, 2));
		if(!fl.isDirectory()) fl.mkdirs();
		fl = new File(fl, hash + ".bin");
		return fl;
	}
	
	public static boolean isHashedOrRequest(String hash)
	{
		if(isHashed(hash)) return true;
		if(!REQUESTED.contains(hash))
		{
			Network.sendToServer(new PacketRequestImageData(hash));
			REQUESTED.add(hash);
		}
		return false;
	}
	
	public static boolean isHashed(String hash)
	{
		return HASHED.computeIfAbsent(hash, h -> getHashFile(h).isFile());
	}
	
	public static String save(ImageData data)
	{
		String hash = data.getHash();
		
		File fl = getHashFile(hash);
		if(fl.isFile()) return hash;
		
		try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(fl)))
		{
			data.write(dos);
		} catch(IOException e)
		{
			OnlineDisplays.LOG.fatal("Failed to save image", e);
			fl.delete();
		}
		
		// Re-compute the hash value.
		if(fl.isFile())
		{
			REQUESTED.remove(hash);
			HASHED.remove(hash);
		}
		
		return hash;
	}
	
	public static ImageData load(String hash)
	{
		if(StringUtils.isNullOrEmpty(hash)) return null;
		
		File fl = getHashFile(hash);
		
		if(fl.isFile())
		{
			try(DataInputStream in = new DataInputStream(new FileInputStream(fl)))
			{
				return new ImageData(in);
			} catch(IOException e)
			{
				OnlineDisplays.LOG.fatal("Failed to read image", e);
			}
		}
		
		return null;
	}
}