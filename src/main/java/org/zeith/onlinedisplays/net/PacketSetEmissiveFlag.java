package org.zeith.onlinedisplays.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.zeith.hammerlib.net.*;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@MainThreaded
public class PacketSetEmissiveFlag
		implements IPacket
{
	BlockPos pos;
	boolean emissive;
	
	public PacketSetEmissiveFlag(TileDisplay pos, boolean emissive)
	{
		this.pos = pos.getBlockPos();
		this.emissive = emissive;
	}
	
	public PacketSetEmissiveFlag()
	{
	}
	
	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBlockPos(pos);
		buf.writeBoolean(emissive);
	}
	
	@Override
	public void read(FriendlyByteBuf buf)
	{
		pos = buf.readBlockPos();
		emissive = buf.readBoolean();
	}
	
	@Override
	public void serverExecute(PacketContext ctx)
	{
		var sender = ctx.getSender();
		if(sender != null)
		{
			TileDisplay d = Cast.cast(sender.level.getBlockEntity(pos), TileDisplay.class);
			if(d != null && d.canEdit(sender))
				d.isEmissive.set(emissive);
		}
	}
}
