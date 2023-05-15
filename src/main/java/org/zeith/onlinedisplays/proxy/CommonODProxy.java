package org.zeith.onlinedisplays.proxy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.api.IImageDataContainer;
import org.zeith.onlinedisplays.client.texture.IDisplayableTexture;
import org.zeith.onlinedisplays.level.LevelImageStorage;
import org.zeith.onlinedisplays.net.TransferImageSession;
import org.zeith.onlinedisplays.net.UploadLocalFileSession;
import org.zeith.onlinedisplays.tiles.TileDisplay;

public class CommonODProxy
{
	public void construct()
	{
	}
	
	public boolean isLocalPlayer(ServerPlayer player)
	{
		return false;
	}
	
	public IImageDataContainer getImageContainer(Level world)
	{
		if(world instanceof ServerLevel sl)
		{
			return LevelImageStorage.get(sl);
		}
		
		return IImageDataContainer.DUMMY;
	}
	
	public IDisplayableTexture resolveTexture(TileDisplay display)
	{
		return null;
	}
	
	public boolean isCurrentlyEditing(TileDisplay display)
	{
		return false;
	}
	
	public boolean isCreative()
	{
		return false;
	}
	
	public void applyClientPicture(TransferImageSession session, PacketContext ctx)
	{
	}
	
	public void uploadServerPicture(UploadLocalFileSession session, PacketContext ctx)
	{
		if(ctx.getSide() == LogicalSide.SERVER)
		{
			if(session.isValid())
			{
				ServerPlayer sender = ctx.getSender();
				if(sender != null)
				{
					TileDisplay display = Cast.cast(sender.level.getBlockEntity(session.getPosition()), TileDisplay.class);
					if(display != null)
					{
						display.setLocalImage(session.getImageData(), session.getFileName(), sender.getName());
					}
				}
			} else
			{
				OnlineDisplays.LOG.error("Failed to load display at " + session.getPosition(), session.getError());
			}
		}
	}
}