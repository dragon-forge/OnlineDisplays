package org.zeith.onlinedisplays.net;

import net.minecraft.core.BlockPos;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraftforge.common.util.LogicalSidedProvider;
import org.zeith.hammerlib.net.MainThreaded;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.net.lft.ITransportAcceptor;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.tiles.TileDisplay;

import java.io.*;
import java.nio.file.Files;

@MainThreaded
public class UploadLocalFileSession
		implements ITransportAcceptor
{
	BlockPos position;
	byte[] imageData;
	String fileName;
	
	Exception error;
	boolean valid;
	
	public static byte[] generate(File file, TileDisplay tile)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		
		try
		{
			out.writeLong(tile.getBlockPos().asLong());
			out.writeUTF(file.getName());
			byte[] image = Files.readAllBytes(file.toPath());
			out.writeInt(image.length);
			out.write(image);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return baos.toByteArray();
	}
	
	@Override
	public void read(InputStream readable, int length)
	{
		try(DataInputStream in = new DataInputStream(readable))
		{
			position = BlockPos.of(in.readLong());
			OnlineDisplays.LOG.info("Reading LOCAL display image for " + position);
			
			fileName = in.readUTF();
			
			imageData = new byte[in.readInt()];
			int r, ptr = 0;
			
			while((r = in.read(imageData, ptr, imageData.length - ptr)) > 0)
			{
				ptr += r;
			}
			
			if(ptr < imageData.length)
			{
				imageData = null;
				throw new EOFException();
			}
			
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
	
	public BlockPos getPosition()
	{
		return position;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public byte[] getImageData()
	{
		return imageData;
	}
	
	public Exception getError()
	{
		return error;
	}
	
	@Override
	public void onTransmissionComplete(PacketContext ctx)
	{
		BlockableEventLoop<?> executor = LogicalSidedProvider.WORKQUEUE.get(ctx.getSide());
		executor.submitAsync(() ->
		{
			OnlineDisplays.PROXY.uploadServerPicture(this, ctx);
		});
	}
}