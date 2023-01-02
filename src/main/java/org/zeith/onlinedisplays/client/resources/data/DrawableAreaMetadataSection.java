package org.zeith.onlinedisplays.client.resources.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import org.zeith.onlinedisplays.OnlineDisplays;

public class DrawableAreaMetadataSection
{
	public static final DrawableAreaMetadataSection.Serializer SERIALIZER = new DrawableAreaMetadataSection.Serializer();
	
	public int x, y, width, height, textureWidth, textureHeight;
	
	public DrawableAreaMetadataSection(JsonObject obj)
	{
		this.x = JSONUtils.getAsInt(obj, "x");
		this.y = JSONUtils.getAsInt(obj, "y");
		this.width = JSONUtils.getAsInt(obj, "width");
		this.height = JSONUtils.getAsInt(obj, "height");
		this.textureWidth = JSONUtils.getAsInt(obj, "tx_width");
		this.textureHeight = JSONUtils.getAsInt(obj, "tx_height");
	}
	
	public static class Serializer
			implements IMetadataSectionSerializer<DrawableAreaMetadataSection>
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