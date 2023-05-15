package org.zeith.onlinedisplays.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.saveddata.SavedData;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.java.Hashers;
import org.zeith.hammerlib.util.java.net.HttpRequest;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.api.IImageDataContainer;
import org.zeith.onlinedisplays.mixins.DimensionDataStorageAccessor;
import org.zeith.onlinedisplays.net.PacketClearRequestFlag;
import org.zeith.onlinedisplays.net.TransferImageSession;
import org.zeith.onlinedisplays.util.ImageData;
import org.zeith.onlinedisplays.util.NetUtil;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LevelImageStorage
		extends SavedData
		implements IImageDataContainer
{
	public static final String DATA_NAME = OnlineDisplays.MOD_ID + ".image_storage";
	
	public static final Hashers HASH = Hashers.SHA1;
	private File imageCacheDir;
	
	public LevelImageStorage(CompoundTag nbt)
	{
		load(nbt);
	}
	
	private void setImageCacheDir(File file)
	{
		imageCacheDir = file;
	}
	
	protected String worldKey;
	protected WeakReference<MinecraftServer> server;
	protected final Map<String, CompletableFuture<String>> downloadTasks = new HashMap<>();
	
	public Optional<String> queueDownload(String url, UUID... sendToPlayerUUIDs)
	{
		CompletableFuture<String> task = downloadTasks.get(url);
		
		if(task != null)
		{
			if(task.isDone())
				return Optional.ofNullable(task.join());
			
			return Optional.empty();
		}
		
		downloadTasks.put(url, CompletableFuture.supplyAsync(() ->
		{
			byte[] data = null;
			String fileName = null;
			
			try
			{
				HttpRequest req = HttpRequest.get(url)
						.userAgent("MinecraftServer " + worldKey)
						.accept("image/*");
				
				fileName = NetUtil.getFileName(req);
				int code = req.code();
				
				OnlineDisplays.LOG.info("GET " + req.url() + " | " + code + " => " + fileName);
				
				if(code / 100 == 2)
					data = req.bytes();
				
				OnlineDisplays.LOG.info("GOT " + (data != null ? data.length : 0) + " bytes from " + req.url());
			} catch(Throwable e)
			{
				e.printStackTrace();
				fileName = null;
				data = null;
			}
			
			if(fileName != null && data != null)
			{
				ImageData id = new ImageData(fileName, data);
				save(id);
				
				MinecraftServer srv = server.get();
				if(srv != null)
				{
					List<UUID> uuids = Arrays.asList(sendToPlayerUUIDs);
					Network.sendToAll(new PacketClearRequestFlag(id.getHash()));
					TransferImageSession.sendTo(id, p -> uuids.contains(p.getUUID()));
				}
				
				return id.getHash();
			}
			
			return null;
		}));
		
		return Optional.empty();
	}
	
	public File getImageCacheDir()
	{
		if(!imageCacheDir.isDirectory())
			imageCacheDir.mkdirs();
		return imageCacheDir;
	}
	
	@Override
	public void save(ImageData data)
	{
		String hash = data.getHash();
		
		File fl = new File(getImageCacheDir(), hash.substring(0, 2));
		if(!fl.isDirectory()) fl.mkdirs();
		fl = new File(fl, hash + ".bin");
		
		if(fl.isFile()) return;
		
		try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(fl)))
		{
			data.write(dos);
		} catch(IOException e)
		{
			OnlineDisplays.LOG.fatal("Failed to save image", e);
			fl.delete();
		}
	}
	
	@Override
	public ImageData load(String hash)
	{
		if(StringUtil.isNullOrEmpty(hash)) return null;
		
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
	public boolean has(String hash)
	{
		if(StringUtil.isNullOrEmpty(hash)) return false;
		
		File fl = new File(getImageCacheDir(), hash.substring(0, 2));
		if(!fl.isDirectory()) fl.mkdirs();
		fl = new File(fl, hash + ".bin");
		
		return fl.isFile();
	}
	
	public void load(CompoundTag nbt)
	{
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		return nbt;
	}
	
	public static LevelImageStorage get(ServerLevel world)
	{
		// ALWAYS work with overworld.
		world = world.getServer().overworld();
		
		LevelImageStorage data = world.getDataStorage().get(LevelImageStorage::new, DATA_NAME);
		
		DimensionDataStorageAccessor a = (DimensionDataStorageAccessor) world.getDataStorage();
		File file = a.callGetDataFile(OnlineDisplays.MOD_ID + File.separator + "image_cache");
		
		String pth = file.getAbsolutePath();
		file = new File(pth.substring(0, pth.length() - 4)); // strip .dat
		
		if(data == null)
		{
			data = new LevelImageStorage(new CompoundTag());
			world.getDataStorage().set(DATA_NAME, data);
		}
		
		data.server = new WeakReference<>(world.getServer());
		data.worldKey = Objects.toString(world.getServer());
		data.setImageCacheDir(file);
		
		return data;
	}
}