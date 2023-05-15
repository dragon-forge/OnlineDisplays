package org.zeith.onlinedisplays.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.zeith.hammerlib.net.*;
import org.zeith.hammerlib.net.packets.SendPropertiesPacket;
import org.zeith.hammerlib.net.properties.PropertyDispatcher;
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
			var level = sender.getLevel();
			TileDisplay display = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(display != null)
			{
				sender.connection.send(display.getUpdatePacket());
//				ctx.withReply(detectAndGenerateChanges(display.getProperties(), display.getBlockPos()));
			} else
				OnlineDisplays.LOG.info("[PacketRequestDisplaySync] Unable to find a display at " + pos);
		}
	}
	
	public SendPropertiesPacket detectAndGenerateChanges(PropertyDispatcher disp, BlockPos pos)
	{
		ByteBuf bb = Unpooled.buffer();
		FriendlyByteBuf buf = new FriendlyByteBuf(bb);
		if(!disp.properties.isEmpty())
		{
			disp.properties.forEach((id, prop) ->
			{
				if(prop.hasChanged())
				{
					buf.writeUtf(id);
					prop.write(buf);
				}
			});
		}
		buf.writeUtf("!");
		int size = bb.writerIndex();
		if(size > 0)
		{
			bb.readerIndex(0);
			byte[] data = new byte[size];
			bb.readBytes(data);
			return new SendPropertiesPacket(pos.asLong(), data);
		}
		return null;
	}
}