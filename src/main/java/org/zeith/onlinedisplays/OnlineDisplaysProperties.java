package org.zeith.onlinedisplays;

import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraftforge.common.util.SortedProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class OnlineDisplaysProperties
		extends PropertyManager<OnlineDisplaysProperties>
{
	public final boolean survivalMode = this.get("survival-mode", false);
	
	public OnlineDisplaysProperties(Properties props)
	{
		super(props);
	}
	
	public void store(Path path)
	{
		try(OutputStream outputstream = Files.newOutputStream(path))
		{
			SortedProperties.store(cloneProperties(), outputstream,
					"Online Display properties\n\n" +
							"To allow obtaining and interacting with the Display, set survival-mode to true.\n" +
							"Please note, however, that making it accessible in survival is generally a BAD idea for public servers.\n" +
							"I personally recommend turning it on ONLY on your personal server, with friends.\n"
			);
		} catch(IOException ioexception)
		{
			OnlineDisplays.LOG.error("Failed to store properties to file: " + path);
		}
	}
	
	@Override
	protected OnlineDisplaysProperties reload(DynamicRegistries regs, Properties props)
	{
		return new OnlineDisplaysProperties(props);
	}
}