package org.zeith.onlinedisplays;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.Settings;
import net.minecraftforge.common.util.SortedProperties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

public class OnlineDisplaysProperties
		extends Settings<OnlineDisplaysProperties>
{
	public final boolean survivalMode = this.get("survival-mode", false);
	
	public OnlineDisplaysProperties(Properties props)
	{
		super(props);
	}
	
	@Override
	public void store(Path path)
	{
		try(var out = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8))
		{
			SortedProperties.store(cloneProperties(), out,
					"""
							Online Display properties
							
							To allow obtaining and interacting with the Display, set survival-mode to true.
							Please note, however, that making it accessible in survival is generally a BAD idea for public servers.
							I personally recommend turning it on ONLY on your personal server, with friends.
							"""
			);
		} catch(IOException ioexception)
		{
			OnlineDisplays.LOG.error("Failed to store properties to file: " + path);
		}
	}
	
	@Override
	protected OnlineDisplaysProperties reload(RegistryAccess regs, Properties props)
	{
		return new OnlineDisplaysProperties(props);
	}
}