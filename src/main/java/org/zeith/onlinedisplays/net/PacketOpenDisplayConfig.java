package org.zeith.onlinedisplays.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.hammerlib.net.*;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.client.gui.GuiDisplayConfig;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@MainThreaded
public class PacketOpenDisplayConfig
		implements INBTPacket
{
	private BlockPos pos;
	private TileDisplay.DisplayMatrix mat;
	private String url;
	
	public PacketOpenDisplayConfig(TileDisplay tile)
	{
		this.pos = tile.getBlockPos();
		this.mat = tile.matrix;
		this.url = tile.imageURL.get();
	}
	
	public PacketOpenDisplayConfig()
	{
	}
	
	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeLong(pos.asLong());
		buf.writeNbt(mat.serializeNBT());
		buf.writeUtf(url != null ? url : "");
	}
	
	@Override
	public void read(PacketBuffer buf)
	{
		pos = BlockPos.of(buf.readLong());
		mat = new TileDisplay.DisplayMatrix();
		mat.deserializeNBT(buf.readNbt());
		url = buf.readUtf();
		if(url.isEmpty()) url = null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		Minecraft mc = Minecraft.getInstance();
		ClientWorld level = mc.level;
		if(level != null)
		{
			TileDisplay display = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(display != null)
			{
				display.matrix.deserializeNBT(mat.serializeNBT());
				display.updateURL(url);
				
				mc.setScreen(new GuiDisplayConfig(display));
			}
		}
	}
}