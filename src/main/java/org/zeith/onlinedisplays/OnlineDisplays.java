package org.zeith.onlinedisplays;

import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.Settings;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.*;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import org.zeith.hammerlib.util.CommonMessages;
import org.zeith.onlinedisplays.ext.gif.ExtGIF;
import org.zeith.onlinedisplays.ext.webp.ExtWebP;
import org.zeith.onlinedisplays.init.BlocksOD;
import org.zeith.onlinedisplays.proxy.*;
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
		CommonMessages.printMessageOnIllegalRedistribution(OnlineDisplays.class,
				LOG, "OnlineDisplays", "https://www.curseforge.com/minecraft/mc-mods/online-displays"
		);
		
		PROXY.construct();
		LanguageAdapter.registerMod(MOD_ID);
		
		var mbus = FMLJavaModLoadingContext.get().getModEventBus();
		
		mbus.addListener(this::processIMC);
		mbus.addListener(this::populateCreative);
		
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		
		getModSettings();
		InterModComms.sendTo(MOD_ID, "add_ext", ExtWebP::new); // Add support for WebP
		InterModComms.sendTo(MOD_ID, "add_ext", ExtGIF::new); // Add support for GIF
	}
	
	public void populateCreative(BuildCreativeModeTabContentsEvent e)
	{
		if(e.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS)
		{
			e.accept(BlocksOD.DISPLAY);
		}
	}
	
	private void registerCommands(RegisterCommandsEvent e)
	{
		e.getDispatcher().register(CommandOnlineDisplay.create());
	}
	
	public static OnlineDisplaysProperties getModSettings()
	{
		if(modSettings == null)
		{
			File props = new File(getModConfigDir(), "main.properties");
			Properties prop;
			if(props.isFile()) prop = Settings.loadFromFile(props.toPath());
			else prop = new Properties();
			modSettings = new OnlineDisplaysProperties(prop);
			modSettings.store(props.toPath());
		}
		return modSettings;
	}
	
	private void processIMC(InterModProcessEvent e)
	{
		e.getIMCStream().forEach(msg ->
		{
			if(msg.getMethod().equalsIgnoreCase("add_ext"))
			{
				Object o = msg.getMessageSupplier().get();
				if(o instanceof ExtensionParser parser)
				{
					LOG.info("Registering extension parser for ." + parser.extension + " files from " +
							msg.getSenderModId());
					EXTENSION_PARSERS.put(parser.extension, parser);
				}
			}
		});
	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MOD_ID, path);
	}
	
	public static Component EMPTY_TXT = Component.literal("");
	
	public static MutableComponent gui(String path)
	{
		return Component.translatable("gui." + MOD_ID + "." + path);
	}
	
	public static MutableComponent info(String path)
	{
		return Component.translatable("info." + MOD_ID + "." + path);
	}
	
	public static File getModConfigDir()
	{
		File f = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).toFile();
		if(!f.isDirectory()) f.mkdirs();
		return f;
	}
	
	public static File getModDir()
	{
		File f = FMLPaths.GAMEDIR.get().resolve("$" + MOD_ID).toFile();
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