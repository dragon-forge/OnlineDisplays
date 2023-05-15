package org.zeith.onlinedisplays.net;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.net.lft.*;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.util.ImageData;

import java.io.*;
import java.util.function.Predicate;

@MainThreaded
public class TransferImageSession
		implements ITransportAcceptor
{
	ImageData imageData;
	
	Exception error;
	boolean valid;
	
	public static void sendTo(ImageData data, Predicate<ServerPlayer> predicate)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		
		try
		{
			data.write(out);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		TransportSession session = new TransportSessionBuilder()
				.setAcceptor(TransferImageSession.class)
				.addData(baos.toByteArray())
				.build();
		
		MinecraftServer mcs = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
		if(mcs != null) for(ServerPlayer mp : mcs.getPlayerList().getPlayers())
			if(predicate.test(mp))
			{
				if(OnlineDisplays.PROXY.isLocalPlayer(mp))
				{
					TransferImageSession local = new TransferImageSession();
					local.valid = true;
					local.imageData = data;
					OnlineDisplays.PROXY.applyClientPicture(local, null);
					OnlineDisplays.LOG.info("Applied image " + data.getHash() + " to host (" + mp.getGameProfile().getName() + ") using memory connection.");
				} else session.sendTo(mp);
			}
	}
	
	@Override
	public void read(InputStream readable, int length)
	{
		try(DataInputStream in = new DataInputStream(readable))
		{
			imageData = new ImageData(in);
			valid = true;
		} catch(Exception e)
		{
			valid = false;
			error = e;
		}
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public Exception getError()
	{
		return error;
	}
	
	public ImageData getImageData()
	{
		return imageData;
	}
	
	@Override
	public void onTransmissionComplete(PacketContext ctx)
	{
		OnlineDisplays.PROXY.applyClientPicture(this, ctx);
	}
}