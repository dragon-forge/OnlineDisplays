package org.zeith.onlinedisplays;

import net.minecraft.server.dedicated.ServerProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import org.zeith.onlinedisplays.ext.gif.ExtGIF;
import org.zeith.onlinedisplays.ext.webp.ExtWebP;
import org.zeith.onlinedisplays.proxy.ClientODProxy;
import org.zeith.onlinedisplays.proxy.CommonODProxy;
import org.zeith.onlinedisplays.util.ExtensionParser;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Mod(OnlineDisplays.MOD_ID)
public class OnlineDisplays
{
	public static final Logger LOG = LogManager.getLogger("OnlineDisplays");
	public static final String MOD_ID = "onlinedisplays";
	
	private static final Map<String, ExtensionParser> EXTENSION_PARSERS = new HashMap<>();
	
	public static final CommonODProxy PROXY = DistExecutor.unsafeRunForDist(() -> ClientODProxy::new, () -> CommonODProxy::new);
	
	public static final Pattern URL_REGEX = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");
	public static final Predicate<String> URL_TEST = URL_REGEX.asPredicate();
	
	private static OnlineDisplaysProperties modSettings;
	
	public OnlineDisplays()
	{
		PROXY.construct();
		LanguageAdapter.registerMod(MOD_ID);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		
		File props = new File(getModConfigDir(), "main.properties");
		Properties prop;
		if(props.isFile()) prop = ServerProperties.loadFromFile(props.toPath());
		else prop = new Properties();
		modSettings = new OnlineDisplaysProperties(prop);
		modSettings.store(props.toPath());
		
		InterModComms.sendTo(MOD_ID, "add_ext", ExtWebP::new); // Add support for WebP
		InterModComms.sendTo(MOD_ID, "add_ext", ExtGIF::new); // Add support for GIF
	}
	
	public static OnlineDisplaysProperties getModSettings()
	{
		return modSettings;
	}
	
	private void processIMC(InterModProcessEvent e)
	{
		e.getIMCStream().forEach(msg ->
		{
			if(msg.getMethod().equalsIgnoreCase("add_ext"))
			{
				Object o = msg.getMessageSupplier().get();
				if(o instanceof ExtensionParser)
				{
					ExtensionParser parser = (ExtensionParser) o;
					LOG.info("Registering extention parser for ." + parser.extension + " files from " + msg.getSenderModId());
					EXTENSION_PARSERS.put(parser.extension, parser);
				}
			}
		});
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
	
	public static TextComponent EMPTY_TXT = new StringTextComponent("");
	
	public static TextComponent gui(String path)
	{
		return new TranslationTextComponent("gui." + MOD_ID + "." + path);
	}
	
	public static TextComponent info(String path)
	{
		return new TranslationTextComponent("info." + MOD_ID + "." + path);
	}
	
	public static File getModConfigDir()
	{
		File f = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).toFile();
		if(!f.isDirectory()) f.mkdirs();
		return f;
	}
	
	public static File getModDir()
	{
		File f = FMLPaths.GAMEDIR.get().resolve("." + MOD_ID).toFile();
		if(!f.isDirectory())
			f.mkdirs();
		return f;
	}
	
	public static ExtensionParser findExtensionParser(String fileName)
	{
		int lastDot = fileName.lastIndexOf('.');
		if(lastDot < 0) return ExtensionParser.IDENTITY;
		
		String ext = fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
		return EXTENSION_PARSERS.getOrDefault(ext, ExtensionParser.IDENTITY);
	}
}