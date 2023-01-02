package org.zeith.onlinedisplays.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.zeith.hammerlib.net.IPacket;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.net.packets.SendPropertiesPacket;
import org.zeith.hammerlib.net.properties.PropertyDispatcher;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.tiles.TileDisplay;

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
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
	}
	
	@Override
	public void read(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
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
				sender.connection.send(display.getUpdatePacket());
//				ctx.withReply(detectAndGenerateChanges(display.getProperties(), display.getBlockPos()));
			} else
				OnlineDisplays.LOG.info("Unable to find a display at " + pos);
		}
	}
	
	public SendPropertiesPacket detectAndGenerateChanges(PropertyDispatcher disp, BlockPos pos)
	{
		ByteBuf bb = Unpooled.buffer();
		PacketBuffer buf = new PacketBuffer(bb);
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