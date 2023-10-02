package org.zeith.onlinedisplays.proxy;

import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import org.zeith.hammerlib.net.*;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.api.IImageDataContainer;
import org.zeith.onlinedisplays.client.texture.IDisplayableTexture;
import org.zeith.onlinedisplays.level.LevelImageStorage;
import org.zeith.onlinedisplays.net.*;
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
				OnlineDisplays.LOG.info("Saving " + session.getFileName() + " byte[" + session.getImageData().length + "] image for " + session.getPosition());
				
				ServerPlayer sender = ctx.getSender();
				if(sender != null)
				{
					var be = sender.level().getBlockEntity(session.getPosition());
					TileDisplay display = Cast.cast(be, TileDisplay.class);
					if(display != null)
					{
						display.setLocalImage(session.getImageData(), session.getFileName(), sender.getName());
						Network.sendTo(sender, new PacketUpdateURL(display));
					} else
						OnlineDisplays.LOG.warn("Unable to find display block at " + session.getPosition() + " (found " + be + ")");
				} else
					OnlineDisplays.LOG.warn("Unable to find image sender for " + session.getPosition());
			} else
			{
				OnlineDisplays.LOG.error("Failed to load display at " + session.getPosition(), session.getError());
			}
		}
	}
}