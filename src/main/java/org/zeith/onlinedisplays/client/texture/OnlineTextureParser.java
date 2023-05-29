package org.zeith.onlinedisplays.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
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
	public static final Map<ResourceLocation, IDisplayableTexture> LOADED_TEXTURES = new HashMap<>();
	
	public static final ITextureFactory TEXTURE_FACTORY = (internalHash, image) ->
	{
		ResourceLocation tex = OnlineDisplays.id("generated/" + internalHash);
		
		IDisplayableTexture cachedTex = LOADED_TEXTURES.get(tex);
		if(cachedTex != null) return cachedTex;
		
		TextureManager txm = Minecraft.getInstance().getTextureManager();
		AbstractTexture tx = txm.getTexture(tex);
		if(!(tx instanceof IDisplayableTexture))
		{
			txm.register(tex, tx = new BufferedTexture(tex, internalHash, () ->
			{
				try(ByteArrayOutputStream out = new ByteArrayOutputStream())
				{
					ImageIO.write(image, "png", out);
					return out.toByteArray();
				} catch(IOException e)
				{
					e.printStackTrace();
					return new byte[0];
				}
			}));
		}
		
		LOADED_TEXTURES.put(tex, Cast.cast(tx, IDisplayableTexture.class));
		return Cast.cast(tx, IDisplayableTexture.class);
	};
	
	public static IDisplayableTexture getTextureByHash(String hash)
	{
		ResourceLocation tex = OnlineDisplays.id("downloaded/" + hash);
		IDisplayableTexture cachedTex = LOADED_TEXTURES.get(tex);
		if(cachedTex != null) return cachedTex;
		
		ImageData image = ClientImageStorage.load(hash);
		if(image == null) return null;
		
		TextureManager txm = Minecraft.getInstance().getTextureManager();
		AbstractTexture tx = txm.getTexture(tex, null);
		
		if(!(tx instanceof BufferedTexture))
		{
			// Performs an attempt to load a custom texture.
			{
				ImageData img = ClientImageStorage.load(hash);
				ExtensionParser parser;
				ImageData converted;
				if(img != null && (parser = OnlineDisplays.findExtensionParser(img.getFileName())).isMatchingBinary(img)
						&& (converted = parser.convert(img)) != null)
				{
					IDisplayableTexture idtx = parser.loadImage(converted, TEXTURE_FACTORY).orElse(null);
					if(idtx != null)
					{
						LOADED_TEXTURES.put(tex, idtx);
						return idtx;
					}
				}
			}
			
			txm.register(tex, tx = new BufferedTexture(tex, hash, () ->
			{
				ImageData img = ClientImageStorage.load(hash);
				if(img == null)
					return new byte[0];
				ExtensionParser parser = OnlineDisplays.findExtensionParser(img.getFileName());
				if(parser.isMatchingBinary(img))
				{
					ImageData imageData = parser.convert(img);
					if(imageData != null)
					{
						img = imageData;
					}
				}
				return img.getData();
			}));
		}
		
		LOADED_TEXTURES.put(tex, Cast.cast(tx, IDisplayableTexture.class));
		return Cast.cast(tx, BufferedTexture.class);
	}
	
	public static IDisplayableTexture getTextureFromIcon(ImageIcon icon)
	{
		String hash = icon.getDescription() + "_" + icon.getIconWidth() + "x" + icon.getIconHeight();
		
		ResourceLocation tex = OnlineDisplays.id("generated/" + hash.hashCode());
		
		IDisplayableTexture cachedTex = LOADED_TEXTURES.get(tex);
		if(cachedTex != null) return cachedTex;
		
		TextureManager txm = Minecraft.getInstance().getTextureManager();
		AbstractTexture tx = txm.getTexture(tex, null);
		
		if(!(tx instanceof IDisplayableTexture))
		{
			txm.register(tex, tx = new BufferedTexture(tex, "", () ->
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
		
		LOADED_TEXTURES.put(tex, Cast.cast(tx, IDisplayableTexture.class));
		return Cast.cast(tx, IDisplayableTexture.class);
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