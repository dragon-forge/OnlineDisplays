package org.zeith.webp4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class WebP
{
	private final File webPExe;
	
	public WebP(File workDir)
	{
		if(!workDir.isDirectory())
			workDir.mkdirs();
		
		this.webPExe = new File(workDir, "webp.exe");
	}
	
	public File getWebPExe() throws IOException
	{
		if(!this.webPExe.isFile())
		{
			String path = "/" + this.getClass().getPackage().getName().replace('.', '/') + "/webp.sup";
			
			try(InputStream in = WebP.class.getResourceAsStream(path))
			{
				Files.copy(in, this.webPExe.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
		
		return this.webPExe;
	}
	
	public Optional<byte[]> convert(File webpFile) throws IOException
	{
		File pngFile = File.createTempFile("onlinedisplays_webp", "tmpnet.png");
		
		int code;
		try
		{
			code = new ProcessBuilder(
					this.getWebPExe().getAbsolutePath(),
					webpFile.getAbsolutePath(),
					"-o",
					pngFile.getAbsolutePath()
			).start().waitFor();
		} catch(InterruptedException var5)
		{
			code = 1;
		}
		
		if(code != 0)
		{
			webpFile.delete();
			pngFile.delete();
			throw new IOException("Unable to convert webp image.");
		} else
		{
			byte[] raster = pngFile.isFile() ? Files.readAllBytes(pngFile.toPath()) : null;
			pngFile.delete();
			return Optional.ofNullable(raster);
		}
	}
}