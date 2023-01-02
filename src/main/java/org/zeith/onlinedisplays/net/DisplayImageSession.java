package org.zeith.onlinedisplays.net;

import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.net.lft.ITransportAcceptor;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.util.ImageData;

import java.io.DataInputStream;
import java.io.InputStream;

@MainThreaded
public class DisplayImageSession
		implements ITransportAcceptor
{
	ImageData imageData;
	
	Exception error;
	boolean valid;
	
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