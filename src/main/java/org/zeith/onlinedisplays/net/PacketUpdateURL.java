package org.zeith.onlinedisplays.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBlockPos(pos);
		buf.writeUtf(url);
	}
	
	@Override
	public void read(FriendlyByteBuf buf)
	{
		pos = buf.readBlockPos();
		url = buf.readUtf();
	}
	
	@Override
	public void serverExecute(PacketContext ctx)
	{
		var sender = ctx.getSender();
		if(sender != null)
		{
			var level = sender.getLevel();
			TileDisplay display = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(display != null)
			{
				display.updateURL(url);
			}
		}
	}
}
