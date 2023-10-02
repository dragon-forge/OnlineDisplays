package org.zeith.onlinedisplays.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.zeith.hammerlib.net.*;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@MainThreaded
public class PacketRequestDisplaySync
		implements IPacket
{
	BlockPos pos;
	
	public PacketRequestDisplaySync(TileDisplay display)
	{
		this.pos = display.getBlockPos();
	}
	
	public PacketRequestDisplaySync()
	{
	}
	
	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBlockPos(pos);
	}
	
	@Override
	public void read(FriendlyByteBuf buf)
	{
		pos = buf.readBlockPos();
	}
	
	@Override
	public void serverExecute(PacketContext ctx)
	{
		var sender = ctx.getSender();
		if(sender != null)
		{
			var level = sender.level();
			TileDisplay display = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(display != null)
			{
				sender.connection.send(display.getUpdatePacket());
//				ctx.withReply(detectAndGenerateChanges(display.getProperties(), display.getBlockPos()));
			} else
				OnlineDisplays.LOG.warn("[PacketRequestDisplaySync] Unable to find a display at " + pos + " (requested update by " + sender.getGameProfile().getName() + ")");
		}
	}
}