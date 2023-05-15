package org.zeith.onlinedisplays.client.resources.data;

import com.google.gson.JsonObject;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.zeith.onlinedisplays.OnlineDisplays;

public class DrawableAreaMetadataSection
{
	public static final DrawableAreaMetadataSection.Serializer SERIALIZER = new DrawableAreaMetadataSection.Serializer();
	
	public int x, y, width, height, textureWidth, textureHeight;
	
	public DrawableAreaMetadataSection(JsonObject obj)
	{
		this.x = GsonHelper.getAsInt(obj, "x");
		this.y = GsonHelper.getAsInt(obj, "y");
		this.width = GsonHelper.getAsInt(obj, "width");
		this.height = GsonHelper.getAsInt(obj, "height");
		this.textureWidth = GsonHelper.getAsInt(obj, "tx_width");
		this.textureHeight = GsonHelper.getAsInt(obj, "tx_height");
	}
	
	public static class Serializer
			implements MetadataSectionSerializer<DrawableAreaMetadataSection>
	{
		@Override
		public String getMetadataSectionName()
		{
			return OnlineDisplays.MOD_ID + ":drawable_area";
		}
		
		@Override
		public DrawableAreaMetadataSection fromJson(JsonObject obj)
		{
			return new DrawableAreaMetadataSection(obj);
		}
	}
}