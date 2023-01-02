package org.zeith.onlinedisplays.util;

import org.zeith.hammerlib.util.java.net.HttpRequest;

import java.nio.file.Paths;

public class NetUtil
{
	public static String getFileName(HttpRequest req)
	{
		String contentDisposition = req.header("Content-Disposition");
		String fileName = null;
		
		if(contentDisposition != null)
		{
			// Extract file name from the Content-Disposition header.
			for(String param : contentDisposition.split(";"))
			{
				if(param.trim().startsWith("filename="))
				{
					fileName = param.substring(param.indexOf("=") + 1);
					fileName = fileName.replace("\"", "");
					break;
				}
			}
		} else
		{
			// Extract file name from the URL.
			fileName = Paths.get(req.url().getPath()).getFileName().toString();
		}
		
		return fileName;
	}
}