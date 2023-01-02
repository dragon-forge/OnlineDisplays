package org.zeith.onlinedisplays.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.zeith.hammerlib.net.PacketContext;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.client.gui.GuiDisplayConfig;
import org.zeith.onlinedisplays.client.texture.*;
import org.zeith.onlinedisplays.net.DisplayImageSession;
import org.zeith.onlinedisplays.tiles.TileDisplay;

public class ClientODProxy
		extends CommonODProxy
{
	@Override
	public void construct()
	{
		super.construct();
		MinecraftForge.EVENT_BUS.addListener(this::clientTick);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addModels);
	}
	
	private void addModels(ModelRegistryEvent e)
	{
		ModelLoader.addSpecialModel(new ResourceLocation(OnlineDisplays.MOD_ID, "item/display_inventory"));
	}
	
	private boolean inLevel;
	
	@Override
	public IDisplayableTexture resolveTexture(TileDisplay display)
	{
		String hash = display.imageHash.get();
		if(hash == null || hash.isEmpty() || !ClientImageStorage.isHashedOrRequest(hash))
			return null;
		return OnlineTextureParser.getTextureByHash(hash);
	}
	
	@Override
	public boolean isCurrentlyEditing(TileDisplay display)
	{
		if(display != null && display.getLevel().isClientSide)
		{
			GuiDisplayConfig gui = Cast.cast(Minecraft.getInstance().screen, GuiDisplayConfig.class);
			if(gui != null && gui.display == display)
				return true;
		}
		
		return super.isCurrentlyEditing(display);
	}
	
	@Override
	public boolean isCreative()
	{
		ClientPlayerEntity player = Minecraft.getInstance().player;
		return player != null && player.isCreative();
	}
	
	private void clientTick(TickEvent.ClientTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END)
		{
			boolean inLevel = Minecraft.getInstance().level != null;
			if(inLevel != this.inLevel)
			{
				this.inLevel = inLevel;
				if(!inLevel)
				{
					OnlineTextureParser.cleanup();
				}
			}
		}
	}
	
	@Override
	public void applyClientPicture(DisplayImageSession session, PacketContext ctx)
	{
		if(ctx.getSide() == LogicalSide.CLIENT)
		{
			if(session.isValid())
			{
				ClientImageStorage.save(session.getImageData());
				OnlineDisplays.LOG.info("Downloaded image " + session.getImageData().getFileName() + " (" + session.getImageData().getData().length + " bytes)");
			} else
			{
				OnlineDisplays.LOG.error("Failed to download image.", session.getError());
			}
			
			return;
		}
		
		super.applyClientPicture(session, ctx);
	}
}