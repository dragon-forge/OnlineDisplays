package org.zeith.onlinedisplays.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.zeith.hammerlib.net.*;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@MainThreaded
public class PacketUpdateTransforms
		implements IPacket
{
	BlockPos pos;
	TileDisplay.DisplayMatrix matrix;
	
	public PacketUpdateTransforms(TileDisplay display)
	{
		this.pos = display.getBlockPos();
		this.matrix = new TileDisplay.DisplayMatrix();
		this.matrix.deserializeNBT(display.matrix.serializeNBT());
	}
	
	public PacketUpdateTransforms()
	{
	}
	
	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBlockPos(pos);
		buf.writeNbt(matrix.serializeNBT());
	}
	
	@Override
	public void read(FriendlyByteBuf buf)
	{
		pos = buf.readBlockPos();
		this.matrix = new TileDisplay.DisplayMatrix();
		matrix.deserializeNBT(buf.readNbt());
	}
	
	@Override
	public void serverExecute(PacketContext ctx)
	{
		var sender = ctx.getSender();
		if(sender != null)
		{
			var level = sender.level();
			TileDisplay display = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(display != null && display.canEdit(sender))
			{
				display.matrix.deserializeNBT(matrix.serializeNBT());
				new PacketRequestDisplaySync(display).serverExecute(ctx);
			}
		}
	}
}
