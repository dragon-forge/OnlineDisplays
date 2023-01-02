package org.zeith.onlinedisplays.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.net.lft.TransportSessionBuilder;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.level.LevelImageStorage;
import org.zeith.onlinedisplays.util.ImageData;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PacketRequestImageData
		implements IPacket
{
	String hash;
	
	public PacketRequestImageData(String hash)
	{
		this.hash = hash;
	}
	
	public PacketRequestImageData()
	{
	}
	
	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeUtf(hash);
	}
	
	@Override
	public void read(PacketBuffer buf)
	{
		hash = buf.readUtf();
	}
	
	@Override
	public void serverExecute(PacketContext ctx)
	{
		ServerPlayerEntity sender = ctx.getSender();
		if(sender != null)
		{
			ServerWorld level = sender.getLevel();
			
			ImageData image = LevelImageStorage.get(level).load(hash);
			if(image != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(baos);
				
				try
				{
					image.write(out);
				} catch(Exception e)
				{
					e.printStackTrace();
				}
				
				new TransportSessionBuilder()
						.setAcceptor(DisplayImageSession.class)
						.addData(baos.toByteArray())
						.build()
						.sendTo(sender);
				
				OnlineDisplays.LOG.info("Sending " + image.getFileName() + " to " + sender.getName().getString());
			} else
				OnlineDisplays.LOG.error("Unable to find " + hash + ", requested by " + sender.getName().getString());
		}
	}
}