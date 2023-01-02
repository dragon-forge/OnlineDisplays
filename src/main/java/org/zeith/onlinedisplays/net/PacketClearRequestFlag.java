package org.zeith.onlinedisplays.net;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.hammerlib.net.*;
import org.zeith.onlinedisplays.client.texture.ClientImageStorage;

@MainThreaded
public class PacketClearRequestFlag
		implements IPacket
{
	String hash;
	
	public PacketClearRequestFlag(String hash)
	{
		this.hash = hash;
	}
	
	public PacketClearRequestFlag()
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
	@OnlyIn(Dist.CLIENT)
	public void clientExecute(PacketContext ctx)
	{
		ClientImageStorage.clearRequestFlag(hash);
	}
}