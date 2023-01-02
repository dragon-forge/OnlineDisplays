package org.zeith.onlinedisplays.net;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
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
	public void write(PacketBuffer buf)
	{
		buf.writeBlockPos(pos);
		buf.writeNbt(matrix.serializeNBT());
	}
	
	@Override
	public void read(PacketBuffer buf)
	{
		pos = buf.readBlockPos();
		this.matrix = new TileDisplay.DisplayMatrix();
		matrix.deserializeNBT(buf.readNbt());
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
				display.matrix.deserializeNBT(matrix.serializeNBT());
				new PacketRequestDisplaySync(display).serverExecute(ctx);
			}
		}
	}
}
