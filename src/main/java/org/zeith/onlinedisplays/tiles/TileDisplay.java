package org.zeith.onlinedisplays.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.hammerlib.api.io.IAutoNBTSerializable;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.properties.PropertyBool;
import org.zeith.hammerlib.net.properties.PropertyString;
import org.zeith.hammerlib.tiles.TileSyncableTickable;
import org.zeith.hammerlib.util.java.DirectStorage;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.client.texture.IDisplayableTexture;
import org.zeith.onlinedisplays.init.TilesOD;
import org.zeith.onlinedisplays.level.LevelImageStorage;
import org.zeith.onlinedisplays.net.PacketClearRequestFlag;
import org.zeith.onlinedisplays.net.PacketRequestDisplaySync;
import org.zeith.onlinedisplays.util.ImageData;

import java.util.Objects;

public class TileDisplay
		extends TileSyncableTickable
{
	@NBTSerializable
	private String url = "";
	
	@NBTSerializable
	private String fileName = "";
	
	@NBTSerializable
	private boolean loaded;
	
	@NBTSerializable
	private boolean emissive;
	
	@NBTSerializable
	public final DisplayMatrix matrix = new DisplayMatrix();
	
	@NBTSerializable
	private String dataHash = "";
	
	public final PropertyString imageURL = new PropertyString(DirectStorage.create($ -> url = $, () -> url));
	public final PropertyString imageHash = new PropertyString(DirectStorage.create($ -> dataHash = $, () -> dataHash));
	public final PropertyBool isLoaded = new PropertyBool(DirectStorage.create($ -> loaded = $, () -> loaded));
	public final PropertyBool isEmissive = new PropertyBool(DirectStorage.create($ -> emissive = $, () -> emissive));
	
	public boolean isSynced;
	
	public IDisplayableTexture image;
	
	public TileDisplay(BlockPos pos, BlockState state)
	{
		super(TilesOD.DISPLAY, pos, state);
		dispatcher.registerProperty("url", imageURL);
		dispatcher.registerProperty("hash", imageHash);
		dispatcher.registerProperty("loaded", isLoaded);
		dispatcher.registerProperty("emissive", isEmissive);
	}
	
	@Override
	public void update()
	{
		if(isOnClient())
		{
			if(!isSynced)
			{
				Network.sendToServer(new PacketRequestDisplaySync(this));
				isSynced = true;
			} else
			{
				if(image == null || !Objects.equals(image.getHash(), imageHash.get()))
				{
					image = OnlineDisplays.PROXY.resolveTexture(this);
				}
			}

//			Network.sendToServer(new PacketRequestImageData(imageHash.get()));
		}
		
		if(isOnServer() && atTickRate(20))
		{
			MinecraftServer server = level.getServer();
			
			if(server != null && level instanceof ServerLevel sw)
			{
				LevelImageStorage storage = LevelImageStorage.get(sw);
				
				if(!StringUtil.isNullOrEmpty(imageURL.get()) && OnlineDisplays.URL_TEST.test(imageURL.get()))
				{
					// We refresh the image once in a while (for our case, once per server lifetime)
					// To ensure we have the most up-to-date image.
					// Local images are unaffected, since they are stored LOCALLY.
					String hash = storage.queueDownload(imageURL.get()).orElse(null);
					if(hash != null) imageHash.set(hash);
				}
				
				final var pkt = getUpdatePacket();
				if(pkt != null)
					sw.getChunkSource().chunkMap.getPlayers(new ChunkPos(worldPosition), false)
							.forEach(e -> e.connection.send(pkt));
			}
		}
	}
	
	public void updateURL(String url)
	{
		updateURL(url, false);
	}
	
	public void updateURL(String url, boolean reDownload)
	{
		if(url == null) url = "";
		
		if(Objects.equals(imageURL.get(), url) && !reDownload)
			return;
		
		this.imageURL.set(url);
		
		if(url.startsWith("local/"))
		{
			isLoaded.set(false);
			imageHash.set(url.substring(6));
			return;
		}
		
		if(!level.isClientSide)
		{
			// Server-side logic
			isLoaded.set(false);
			imageHash.set("");
		}
	}
	
	public void setLocalImage(byte[] newValue, String fileName, Object sender)
	{
		isLoaded.set(newValue != null && newValue.length > 0);
		
		if(newValue == null)
		{
			imageHash.set("");
			return;
		}
		
		ImageData id = new ImageData(fileName, newValue);
		if(level instanceof ServerLevel sl)
			LevelImageStorage.get(sl).save(id);
		
		String hash = id.getHash();
		updateURL("local/" + hash);
		imageHash.set(hash);
		Network.sendToAll(new PacketClearRequestFlag(hash));
		sync();
		
		OnlineDisplays.LOG.info("Uploaded " + fileName + " (" + hash + ") from " + sender);
	}
	
	public void updateCache(byte[] newValue, String fileName)
	{
		isLoaded.set(newValue != null && newValue.length > 0);
		
		if(newValue == null)
		{
			imageHash.set("");
			return;
		}
		
		ImageData id = new ImageData(fileName, newValue);
		if(level instanceof ServerLevel sl)
			LevelImageStorage.get(sl).save(id);
		
		String hash = id.getHash();
		imageHash.set(hash);
		Network.sendToAll(new PacketClearRequestFlag(hash));
		sync();
		
		OnlineDisplays.LOG.info("Downloaded " + fileName + " (" + hash + ")");
	}
	
	@Override
	public AABB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet)
	{
		if(OnlineDisplays.PROXY.isCurrentlyEditing(this))
			return;
		super.onDataPacket(net, packet);
	}
	
	public static class DisplayMatrix
			implements IAutoNBTSerializable
	{
		@NBTSerializable("sx")
		public float scaleX = 1;
		
		@NBTSerializable("sy")
		public float scaleY = 1;
		
		@NBTSerializable("tx")
		public float translateX;
		
		@NBTSerializable("ty")
		public float translateY;
		
		@NBTSerializable("tz")
		public float translateZ;
		
		@NBTSerializable("rx")
		public float rotateX;
		
		@NBTSerializable("ry")
		public float rotateY;
		
		@NBTSerializable("rz")
		public float rotateZ;
		
		@OnlyIn(Dist.CLIENT)
		public void apply(PoseStack pose)
		{
			pose.translate(translateX, translateY, translateZ);
			
			pose.mulPose(Vector3f.YP.rotationDegrees(rotateY));
			pose.mulPose(Vector3f.XP.rotationDegrees(rotateX));
			pose.mulPose(Vector3f.ZP.rotationDegrees(rotateZ));
			
			pose.scale(scaleX, scaleY, 1F);
		}
	}
}