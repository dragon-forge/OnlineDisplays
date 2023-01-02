package org.zeith.onlinedisplays.level;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.StringUtils;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import org.zeith.hammerlib.util.java.Hashers;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.mixins.DimensionSavedDataManagerAccessor;
import org.zeith.onlinedisplays.util.ImageData;

import java.io.*;

public class LevelImageStorage
		extends WorldSavedData
{
	public static final String DATA_NAME = OnlineDisplays.MOD_ID + ".image_storage";
	
	public static final Hashers HASH = Hashers.SHA1;
	private File imageCacheDir;
	
	public LevelImageStorage()
	{
		super(DATA_NAME);
	}
	
	private void setImageCacheDir(File file)
	{
		imageCacheDir = file;
	}
	
	public File getImageCacheDir()
	{
		if(!imageCacheDir.isDirectory())
			imageCacheDir.mkdirs();
		return imageCacheDir;
	}
	
	public String save(ImageData data)
	{
		String hash = data.getHash();
		
		File fl = new File(getImageCacheDir(), hash.substring(0, 2));
		if(!fl.isDirectory()) fl.mkdirs();
		fl = new File(fl, hash + ".bin");
		
		if(fl.isFile()) return hash;
		
		try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(fl)))
		{
			data.write(dos);
		} catch(IOException e)
		{
			OnlineDisplays.LOG.fatal("Failed to save image", e);
			fl.delete();
		}
		
		return hash;
	}
	
	public ImageData load(String hash)
	{
		if(StringUtils.isNullOrEmpty(hash)) return null;
		
		File fl = new File(getImageCacheDir(), hash.substring(0, 2));
		if(!fl.isDirectory()) fl.mkdirs();
		fl = new File(fl, hash + ".bin");
		
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
	
	@Override
	public void load(CompoundNBT nbt)
	{
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt)
	{
		return nbt;
	}
	
	public static LevelImageStorage get(ServerWorld world)
	{
		LevelImageStorage data = world.getDataStorage().get(LevelImageStorage::new, DATA_NAME);
		
		DimensionSavedDataManagerAccessor a = (DimensionSavedDataManagerAccessor) world.getDataStorage();
		File file = a.callGetDataFile(OnlineDisplays.MOD_ID + File.separator + "image_cache");
		
		String pth = file.getAbsolutePath();
		file = new File(pth.substring(0, pth.length() - 4)); // strip .dat
		
		if(data == null) data = new LevelImageStorage();
		
		data.setImageCacheDir(file);
		return data;
	}
}