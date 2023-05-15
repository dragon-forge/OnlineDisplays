package org.zeith.onlinedisplays.net;

import net.minecraft.network.FriendlyByteBuf;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.level.LevelImageStorage;
import org.zeith.onlinedisplays.util.ImageData;

import java.util.Locale;
import java.util.UUID;

public class PacketRequestImageData
		implements IPacket
{
	String hash;
	String optURL;
	
	public PacketRequestImageData(String hash, String optURL)
	{
		this.hash = hash;
		this.optURL = optURL;
	}
	
	public PacketRequestImageData()
	{
	}
	
	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeUtf(hash);
		buf.writeUtf(optURL);
	}
	
	@Override
	public void read(FriendlyByteBuf buf)
	{
		hash = buf.readUtf();
		optURL = buf.readUtf();
	}
	
	@Override
	public void serverExecute(PacketContext ctx)
	{
		var sender = ctx.getSender();
		if(sender != null)
		{
			var level = sender.getLevel();
			
			ImageData image = LevelImageStorage.get(level).load(hash);
			if(image != null)
			{
				UUID id = sender.getUUID();
				TransferImageSession.sendTo(image, p -> p.getUUID().equals(id));
				OnlineDisplays.LOG.info("Sending " + image.getFileName() + " to " + sender.getName().getString());
			} else
			{
				OnlineDisplays.LOG.error("Unable to find " + hash + ", requested by " + sender.getName().getString());
				if(optURL != null && !optURL.isEmpty() && !optURL.toLowerCase(Locale.ROOT).startsWith("local/"))
				{
					OnlineDisplays.LOG.info("Downloading hash from " + optURL);
					LevelImageStorage.get(level).queueDownload(optURL, sender.getUUID());
				}
			}
		}
	}
}