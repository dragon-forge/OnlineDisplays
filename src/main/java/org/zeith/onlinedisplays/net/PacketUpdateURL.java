package org.zeith.onlinedisplays.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.zeith.hammerlib.net.*;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@MainThreaded
public class PacketUpdateURL
		implements IPacket
{
	BlockPos pos;
	String url;
	
	public PacketUpdateURL(TileDisplay display)
	{
		this.pos = display.getBlockPos();
		this.url = display.imageURL.get();
	}
	
	public PacketUpdateURL()
	{
	}
	
	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeUtf(url);
	}
	
	@Override
	public void read(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		url = buf.readUtf();
	}
	
	@Override
	public void serverExecute(PacketContext ctx)
	{
		ServerPlayerEntity sender = ctx.getSender();
		if(sender != null)
		{
			ServerWorld level = sender.getLevel();
			TileDisplay display = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(display != null)
			{
				display.updateURL(url);
			}
		}
	}
}
