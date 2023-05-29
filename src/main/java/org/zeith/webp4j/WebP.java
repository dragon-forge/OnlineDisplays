package org.zeith.webp4j;

import org.zeith.onlinedisplays.OnlineDisplays;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;

public class WebP
{
	private final File webPExe;
	
	public WebP(File workDir)
	{
		if(!workDir.isDirectory())
			workDir.mkdirs();
		
		var isWindows = OSArch.getArchitecture().getType() == OSArch.OSType.WINDOWS;
		
		this.webPExe = new File(workDir, "dwebp" + (isWindows ? ".exe" : ""));
	}
	
	public File getWebPExe() throws IOException
	{
		if(!this.webPExe.isFile())
		{
			String file = "windows";
			
			var arch = OSArch.getArchitecture();
			if(arch.getType() == OSArch.OSType.UNIX) file = "linux";
			else if(arch == OSArch.ArchDistro.MACOS_INTEL) file = "mac_intel";
			else if(arch == OSArch.ArchDistro.MACOS_APPLE) file = "mac_arm";
			
			try(InputStream in = WebP.class.getResourceAsStream("/assets/onlinedisplays/dwebp/" + file))
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
			var webp = getWebPExe().getAbsoluteFile();
			var webpPath = webp.getCanonicalPath();
			
			if(!webp.canExecute()) // macos crap.
			{
				if(!webp.setExecutable(true))
				{
					OnlineDisplays.LOG.warn("Unable to make " + webpPath + " as executable via java, attempting chmod.");
					try
					{
						ProcessBuilder chmodProcessBuilder = new ProcessBuilder("chmod", "+x", webpPath);
						Process chmodProcess = chmodProcessBuilder.start();
						int chmodExitCode = chmodProcess.waitFor();
						if(chmodExitCode != 0) throw new IOException("Failed to add executable permissions to the app file: " + webpPath);
					} catch(IOException | InterruptedException e)
					{
						throw e instanceof IOException ie ? ie : new IOException(e);
					}
				}
			}
			
			String[] command = {
					webpPath,
					webpFile.getAbsoluteFile().getCanonicalPath(),
					"-o",
					pngFile.getAbsoluteFile().getCanonicalPath()
			};
			
			OnlineDisplays.LOG.info("Launching dwebp with command: " + Arrays.toString(command));
			
			code = new ProcessBuilder(command).start().waitFor();
		} catch(InterruptedException ie)
		{
			throw new IOException(ie);
		}
		
		if(code != 0 || !pngFile.isFile())
		{
			webpFile.delete();
			pngFile.delete();
			throw new IOException("Unable to convert webp image. Code " + code);
		} else
		{
			byte[] raster = pngFile.isFile() ? Files.readAllBytes(pngFile.toPath()) : null;
			pngFile.delete();
			return Optional.ofNullable(raster);
		}
	}
}