package org.zeith.onlinedisplays.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.util.ExtensionParser;
import org.zeith.onlinedisplays.util.ImageData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OnlineTextureParser
{
	public static final Map<ResourceLocation, Long> LOADED_TEXTURES = new HashMap<>();
	
	public static IDisplayableTexture getTextureFromIcon(ImageIcon icon)
	{
		String hash = icon.getDescription() + "_" + icon.getIconWidth() + "x" + icon.getIconHeight();
		
		ResourceLocation tex = OnlineDisplays.id("generated/" + hash.hashCode());
		TextureManager txm = Minecraft.getInstance().getTextureManager();
		Texture tx = txm.getTexture(tex);
		if(!(tx instanceof BufferedTexture))
		{
			txm.register(tex, new BufferedTexture(tex, "", () ->
			{
				try(ByteArrayOutputStream out = new ByteArrayOutputStream())
				{
					BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = img.createGraphics();
					icon.paintIcon(null, graphics, 0, 0);
					graphics.dispose();
					
					ImageIO.write(img, "png", out);
					
					return out.toByteArray();
				} catch(IOException e)
				{
					e.printStackTrace();
					return new byte[0];
				}
			}));
		}
		LOADED_TEXTURES.put(tex, System.currentTimeMillis()); // Update texture usage timestamp.
		return Cast.cast(tx, BufferedTexture.class);
	}
	
	public static IDisplayableTexture getTextureByHash(String hash)
	{
		ImageData image = ClientImageStorage.load(hash);
		if(image == null) return null;
		
		ResourceLocation tex = OnlineDisplays.id("downloaded/" + hash);
		TextureManager txm = Minecraft.getInstance().getTextureManager();
		Texture tx = txm.getTexture(tex);
		if(!(tx instanceof BufferedTexture))
		{
			txm.register(tex, new BufferedTexture(tex, hash, () ->
			{
				ImageData img = ClientImageStorage.load(hash);
				if(img == null)
					return new byte[0];
				ExtensionParser parser = OnlineDisplays.findExtensionParser(img.getFileName());
				if(parser.isMatchingBinary(img))
				{
					ImageData imageData = parser.toPNG(img);
					if(imageData != null)
						img = imageData;
				}
				return img.getData();
			}));
		}
		LOADED_TEXTURES.put(tex, System.currentTimeMillis()); // Update texture usage timestamp.
		return Cast.cast(tx, BufferedTexture.class);
	}
	
	public static void cleanup()
	{
		OnlineDisplays.LOG.info("Cleanup " + LOADED_TEXTURES.size() + " images.");
		TextureManager txm = Minecraft.getInstance().getTextureManager();
		for(ResourceLocation tex : LOADED_TEXTURES.keySet()) txm.release(tex);
		
		LOADED_TEXTURES.clear();
		ClientImageStorage.REQUESTED.clear();
		ClientImageStorage.HASHED.clear();
	}
}